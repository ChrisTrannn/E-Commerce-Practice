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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
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
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String starName = request.getParameter("starName");
            String genre_id = request.getParameter("genre_id");
            String title_id = request.getParameter("title_id");
            String sortParam = request.getParameter("sort");
            String pageNumStr = request.getParameter("pageNum");
            String perPageStr = request.getParameter("perPage");
            int pageNum = (pageNumStr != null) ? Integer.parseInt(pageNumStr) : 1;
            int perPage = (perPageStr != null) ? Integer.parseInt(perPageStr) : 10;

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
                    "     LIMIT 3 " +
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
                    "WHERE 1=1";

            // add filtering conditions
            if (title != null && !title.isEmpty()) {
                query += " AND MATCH (m.title) AGAINST (? IN BOOLEAN MODE)";
            }
            if (year != null && !year.isEmpty()) {
                query += " AND m.year = ?";
            }
            if (director != null && !director.isEmpty()) {
                query += " AND m.director LIKE ?";
            }
            if (starName != null && !starName.isEmpty()) {
                query += " AND s.name LIKE ?";
            }
            if (genre_id != null && !genre_id.isEmpty()) {
                query += " AND g.id = ?";
            }
            if (title_id != null && !title_id.isEmpty()) {
                // Handle the '*' case: match titles that start with non-alphanumeric characters
                if (title_id.equals("*")) {
                    query += " AND m.title REGEXP '^[^a-zA-Z0-9]'";
                } else {
                    query += " AND m.title LIKE ?";
                }
            }

            if (sortParam != null && !sortParam.isEmpty()) {
                switch (sortParam) {
                    case "titleAsc":
                        query += " ORDER BY m.title ASC, r.rating ASC";
                        break;
                    case "titleDesc":
                        query += " ORDER BY m.title DESC, r.rating ASC";
                        break;
                    case "ratingAsc":
                        query += " ORDER BY r.rating ASC, m.title DESC";
                        break;
                    case "ratingDesc":
                        query += " ORDER BY r.rating DESC, m.title ASC";
                        break;
                    case "titleRatingAsc":
                        query += " ORDER BY m.title ASC, r.rating ASC";
                        break;
                    case "titleRatingDesc":
                        query += " ORDER BY m.title DESC, r.rating ASC";
                        break;
                    case "ratingTitleAsc":
                        query += " ORDER BY r.rating ASC, m.title ASC";
                        break;
                    case "ratingTitleDesc":
                        query += " ORDER BY r.rating DESC, m.title ASC";
                        break;
                    default:
                        break;
                }
            }

            int offset = (pageNum - 1) * perPage;
            query += " LIMIT ? OFFSET ?;";
            System.out.println(Integer.toString(pageNum) + ' ' + Integer.toString(perPage));

            PreparedStatement statement = conn.prepareStatement(query);


            int parameterIndex = 1;
            if (title != null && !title.isEmpty()) {
                // adds asterisk to the end and plus to front of every word to satisfy prefix boolean condition
                StringBuilder titleBuilder = new StringBuilder();
                for (String word: title.split(" ")) {
                    titleBuilder.append("+").append(word).append("* ");
                }
                statement.setString(parameterIndex++, titleBuilder.toString());
            }
            if (year != null && !year.isEmpty()) {
                statement.setInt(parameterIndex++, Integer.parseInt(year));
            }
            if (director != null && !director.isEmpty()) {
                statement.setString(parameterIndex++, "%" + director + "%");
            }
            if (starName != null && !starName.isEmpty()) {
                statement.setString(parameterIndex++, "%" + starName + "%");
            }
            if (genre_id != null && !genre_id.isEmpty()) {
                statement.setInt(parameterIndex++, Integer.parseInt(genre_id));
            }
            if (title_id != null && !title_id.isEmpty() && !title_id.equals("*")) {
                statement.setString(parameterIndex++, title_id + "%");
            }
            statement.setInt(parameterIndex++, perPage);
            statement.setInt(parameterIndex, offset);

            // print the query
            System.out.println("Query: " + query);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            // Create a JsonArray to hold the data we retrieve from rs
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
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