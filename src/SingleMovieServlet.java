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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            String query = "SELECT DISTINCT " +
                    "    m.id AS movie_id, " +
                    "    m.title, " +
                    "    m.year, " +
                    "    m.director, " +
                    "    r.rating AS rating, " +
                    "    (SELECT GROUP_CONCAT(DISTINCT CONCAT(g.id, ':', g.name) ORDER BY g.name SEPARATOR ', ') " +
                    "     FROM genres_in_movies AS gm " +
                    "     INNER JOIN genres AS g ON gm.genreId = g.id " +
                    "     WHERE gm.movieId = m.id) AS genres, " +
                    "    (SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) ORDER BY stars_in_movies_count DESC, s.name ASC SEPARATOR ', ') " +
                    "     FROM ( " +
                    "         SELECT starId, COUNT(*) AS stars_in_movies_count " +
                    "         FROM stars_in_movies " +
                    "         GROUP BY starId " +
                    "     ) AS sm " +
                    "     INNER JOIN stars AS s ON sm.starId = s.id " +
                    "     WHERE sm.starId IN (SELECT starId FROM stars_in_movies WHERE movieId = m.id) " +
                    "     ORDER BY stars_in_movies_count DESC, s.name ASC " +
                    "    ) AS stars, " +
                    "    (SELECT COUNT(DISTINCT sm.starId) " +
                    "     FROM stars_in_movies AS sm " +
                    "     WHERE sm.movieId = m.id) AS movie_count " +
                    "FROM " +
                    "    movies AS m " +
                    "LEFT JOIN " +
                    "    ratings AS r ON m.id = r.movieId " +
                    "INNER JOIN " +
                    "    genres_in_movies AS gm ON m.id = gm.movieId " +
                    "INNER JOIN " +
                    "    genres AS g ON gm.genreId = g.id " +
                    "INNER JOIN " +
                    "    stars_in_movies AS sim ON m.id = sim.movieId " +
                    "INNER JOIN " +
                    "    stars AS s ON sim.starId = s.id " +
                    "WHERE m.id = ? " +
                    "ORDER BY r.rating DESC;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
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

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
