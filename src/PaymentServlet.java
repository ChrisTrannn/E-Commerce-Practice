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

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
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