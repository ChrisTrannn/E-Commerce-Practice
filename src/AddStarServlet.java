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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/add-star")
public class AddStarServlet extends HttpServlet {
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
        String star_name = request.getParameter("star_name");
        String star_birth_year = request.getParameter("star_birth_year");

        // insert the star into the database
        try (Connection conn = dataSource.getConnection()) {
            // execute add_star stored procedure query
            String callStoredProcedure = "{CALL add_star(?, ?, ?)}";
            CallableStatement statement = conn.prepareCall(callStoredProcedure);
            statement.setString(1, star_name);
            if (star_birth_year != null && !star_birth_year.isEmpty()) {
                statement.setInt(2, Integer.parseInt(star_birth_year));
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            statement.registerOutParameter(3, java.sql.Types.VARCHAR);
            statement.executeUpdate();

            // Retrieve the generated star ID
            String newStarId = statement.getString(3);

            // write a response object indicating success
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "Success!");
            responseJsonObject.addProperty("message", "Star Id: " + newStarId);
            request.getServletContext().log("Add Star Success");
            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {
            response.setStatus(500);
            e.printStackTrace();
        }
    }
}