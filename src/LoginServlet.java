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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // check if the username and password, match email and password column from customers table
        try (Connection conn = dataSource.getConnection()) {
            // query the customers table and check if the username and password match
            String query = "SELECT * from customers where email = ? and password = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();

            // iterate through the result set and check if the username and password match
            while (rs.next()) {
                String email = rs.getString("email");
                String passwd = rs.getString("password");

                // Login success if there's a match
                if (email.equals(username) && passwd.equals(password)) {
                    // set this user into the session, set customerId into session as well
                    request.getSession().setAttribute("user", new User(username));
                    request.getSession().setAttribute("customerId", rs.getString("id"));

                    // send success message
                    JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                    response.getWriter().write(responseJsonObject.toString());
                    return;
                }
            }

            // Login fail if there's no match in result set
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "incorrect username or password");
            request.getServletContext().log("Login failed");
            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}