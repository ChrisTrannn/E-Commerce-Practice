import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/_dashboard/api/employee-login")
public class EmployeeLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
    private DataSource masterDataSource;
    private DataSource slaveDataSource;
    private Random rand = new Random();

    public void init(ServletConfig config) {
        try {
            masterDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbMaster");
            slaveDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private DataSource getRandomDataSource() {
        return rand.nextBoolean() ? masterDataSource : slaveDataSource;
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        dataSource = getRandomDataSource();

        // Verify reCAPTCHA
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "reCAPTCHA verification failed");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // check if the username and password, match email and password column from customers table
        try (Connection conn = dataSource.getConnection()) {
            // query the employees table and check if the username and password match
            String query = "SELECT * from employees where email = ? and password = ?";
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
                    // set this user into the session
                    request.getSession().setAttribute("user", new User(username));
                    request.getSession().setAttribute("isEmployee", true);

                    // send success message
                    JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                    response.getWriter().write(responseJsonObject.toString());
                    return;
                }
            }

            // Login fail if there's no match in result set
            // query the employee table and check if the username and password match
            String query2 = "SELECT * from employees where email = ?";
            PreparedStatement statement2 = conn.prepareStatement(query);
            statement2.setString(1, username);
            ResultSet rs2 = statement2.executeQuery();

            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");

            // if username doesn't exist in the database
            if (!rs2.next()) {
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            } else {
                responseJsonObject.addProperty("message", "incorrect password");
            }

            request.getServletContext().log("Login failed");
            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}