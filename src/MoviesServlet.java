import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT m.id AS movie_id, m.title, m.year, m.director, r.rating AS rating, " +
                    "(SELECT GROUP_CONCAT(DISTINCT g.name SEPARATOR ', ') FROM genres_in_movies AS gm " +
                        "INNER JOIN genres AS g ON gm.genreId = g.id " +
                        "WHERE gm.movieId = m.id) AS genres, " +
                    "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) SEPARATOR ', ') FROM " +
                        "(SELECT starId FROM stars_in_movies WHERE movieId = m.id LIMIT 3 ) AS sm " +
                            "INNER JOIN stars AS s ON sm.starId = s.id) AS stars " +
                    "FROM movies AS m " +
                    "INNER JOIN ratings AS r ON m.id = r.movieId " +
                    "ORDER BY r.rating DESC " +
                    "LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                String genres = rs.getString("genres");
                String stars = rs.getString("stars");
                double rating = rs.getDouble("rating");

                // Split the stars result, create star objects, and add them to a stars array
                String[] starArray = stars.split(", ");
                JsonArray starsArray = new JsonArray();

                for (String star: starArray) {
                    String[] starInfo = star.split(":");
                    JsonObject starObject = new JsonObject();
                    starObject.addProperty("id", starInfo[0]);
                    starObject.addProperty("name", starInfo[1]);
                    starsArray.add(starObject);
                }

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("title", title);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("genres", genres);
                jsonObject.add("stars", starsArray);
                jsonObject.addProperty("rating", rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
