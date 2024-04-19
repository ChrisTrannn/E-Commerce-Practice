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
import java.util.Enumeration;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/movies-search"
@WebServlet(name = "SearchMovieServlet", urlPatterns = "/api/search")
public class SearchMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;

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
        // Getting the request URL
        String requestUrl = request.getRequestURL().toString();

        // Getting the query string
        String queryString = request.getQueryString();
        if (queryString != null) {
            // Append the query string to the request URL
            requestUrl += "?" + queryString;
        }

        System.out.println("Request URL: " + requestUrl);
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            Statement statement = conn.createStatement();

            String title = java.net.URLDecoder.decode(request.getParameter("title"), "UTF-8");
            String year = java.net.URLDecoder.decode(request.getParameter("year"), "UTF-8");
            String director = java.net.URLDecoder.decode(request.getParameter("director"), "UTF-8");
            String starName = java.net.URLDecoder.decode(request.getParameter("starName"), "UTF-8");

            System.out.println(title);
            System.out.println(year);
            System.out.println(director);
            System.out.println(starName);

            String query = "SELECT DISTINCT m.id AS movie_id, m.title, m.year, m.director, r.rating AS rating, " +
                    "(SELECT GROUP_CONCAT(DISTINCT g.name SEPARATOR ', ') FROM genres_in_movies AS gm " +
                    "INNER JOIN genres AS g ON gm.genreId = g.id " +
                    "WHERE gm.movieId = m.id) AS genres, " +
                    "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) SEPARATOR ', ') FROM " +
                    "(SELECT starId FROM stars_in_movies WHERE movieId = m.id LIMIT 3 ) AS sm " +
                    "INNER JOIN stars AS s ON sm.starId = s.id) AS stars " +
                    "FROM movies AS m " +
                    "INNER JOIN ratings AS r ON m.id = r.movieId " +
                    "LEFT JOIN stars_in_movies AS sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars AS s ON sim.starId = s.id " +
                    "WHERE 1=1";

            if (title != null && !title.isEmpty()) {
                query += " AND m.title LIKE '%" + title + "%'";
            }
            if (year != null && !year.isEmpty()) {
                query += " AND m.year = " + year;
            }
            if (director != null && !director.isEmpty()) {
                query += " AND m.director LIKE '%" + director + "%'";
            }
            if (starName != null && !starName.isEmpty()) {
                query += " AND s.name LIKE '%" + starName + "%'";
            }

            query += ";";

            System.out.println(query);
            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String movieId = rs.getString("movie_id");
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
                jsonObject.addProperty("title", rs.getString("title"));
                jsonObject.addProperty("year", rs.getString("year"));
                jsonObject.addProperty("director", rs.getString("director"));
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
        }  catch (Exception e) {

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





























