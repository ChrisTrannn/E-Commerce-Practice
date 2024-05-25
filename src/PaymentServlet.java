import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbMaster");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // send data as json response like {movies: [{saleid, movietitle, quantity, price, totalprice}]}
        try (Connection conn = dataSource.getConnection()) {
            // get customerId from session
            String customerId = request.getSession().getAttribute("customerId").toString();

            // query the sales table and get the saleId, movieId, movieQuantity
            String query = "SELECT * from sales where customerId = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, customerId);
            ResultSet rs = statement.executeQuery();

            // iterate through the result set and get the saleId, movieId, movieQuantity
            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String saleId = rs.getString("id");
                String movieId = rs.getString("movieId");
                String movieQuantity = rs.getString("movieQuantity");

                // query the movies table and get the movieTitle, moviePrice
                String query2 = "SELECT m.title as title, mp.price as price from movies as m, movie_and_price as mp where id = ? AND m.id = mp.movieId";
                PreparedStatement statement2 = conn.prepareStatement(query2);
                statement2.setString(1, movieId);
                ResultSet rs2 = statement2.executeQuery();

                // iterate through the result set and get the movieTitle, moviePrice
                while (rs2.next()) {
                    String movieTitle = rs2.getString("title");
                    String moviePrice = rs2.getString("price");

                    // calculate the total price
                    double totalPrice = Double.parseDouble(moviePrice) * Double.parseDouble(movieQuantity);

                    // create a json object and add it to the json array
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("saleId", saleId);
                    jsonObject.addProperty("movieTitle", movieTitle);
                    jsonObject.addProperty("quantity", movieQuantity);
                    jsonObject.addProperty("price", moviePrice);
                    jsonObject.addProperty("totalPrice", totalPrice);
                    jsonArray.add(jsonObject);
                }
            }

            // send the json array as response
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.add("movies", jsonArray);
            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {
            response.setStatus(500);
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String card_first_name = request.getParameter("card_first_name");
        String card_last_name = request.getParameter("card_last_name");
        String card_number = request.getParameter("card_number");
        String card_expiration_date = request.getParameter("card_expiration_date");

        // check if the credit card information can be found in the credit card table
        try (Connection conn = dataSource.getConnection()) {
            // query the credit card table and check if the credit card information can be found
            String query = "SELECT * from creditcards where firstName = ? and lastName = ? and id = ? and expiration = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, card_first_name);
            statement.setString(2, card_last_name);
            statement.setString(3, card_number);
            statement.setString(4, card_expiration_date);
            ResultSet rs = statement.executeQuery();

            // iterate through the result set and check if the credit card information can be found
            while (rs.next()) {
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String id = rs.getString("id");
                String expiration = rs.getString("expiration");

                // Payment success if there's a match
                if (firstName.equals(card_first_name) && lastName.equals(card_last_name) && id.equals(card_number) && expiration.equals(card_expiration_date)) {
                    // send success message
                    JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                    response.getWriter().write(responseJsonObject.toString());

                    // add record into sales table, attributes are id, customerId, movieId, saleDate, movieQuantity
                    String customerId = request.getSession().getAttribute("customerId").toString();

                    // iterate through previousItems in session and add record into sales table
                    ArrayList<String> previousItems = (ArrayList<String>) request.getSession().getAttribute("previousItems");
                    for (String item : previousItems) {
                        JsonObject itemJson = new Gson().fromJson(item, JsonObject.class);
                        String movieId = itemJson.get("movieId").getAsString();
                        String movieQuantity = itemJson.get("quantity").getAsString();

                        // add record into sales table
                        String insertQuery = "INSERT INTO sales (customerId, movieId, saleDate, movieQuantity) VALUES (?, ?, CURDATE(), ?)";
                        statement = conn.prepareStatement(insertQuery);
                        statement.setString(1, customerId);
                        statement.setString(2, movieId);
                        statement.setString(3, movieQuantity);
                        statement.executeUpdate();
                    }

                    return;
                }
            }

            // Payment fail if there's no match in result set
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "incorrect credit card information");
            request.getServletContext().log("Payment failed");
            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}