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

@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/api/add-movie")
public class AddMovieServlet extends HttpServlet {
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
        String movie_title = request.getParameter("movie_title");
        String movie_year = request.getParameter("movie_year");
        String movie_director = request.getParameter("movie_director");
        String star_name = request.getParameter("star_name");
        String star_birth_year = request.getParameter("star_birth_year");
        String genre_name = request.getParameter("genre_name");

        // check if movie is a duplicate by checking if the movie title, director, year already exists
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM movies WHERE title = ? AND director = ? AND year = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, movie_title);
            statement.setString(2, movie_director);
            statement.setInt(3, Integer.parseInt(movie_year));
            ResultSet rs = statement.executeQuery();

            // if the movie exists, return a response object indicating failure
            if (rs.next()) {
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "Error!");
                responseJsonObject.addProperty("message", "Movie already exists!");
                response.getWriter().write(responseJsonObject.toString());
                return;
            }
        } catch (Exception e) {
            response.setStatus(500);
            e.printStackTrace();
        }

        // insert the movie into the database
        try (Connection conn = dataSource.getConnection()) {
            // execute add_movie stored procedure query
            String callStoredProcedure = "{CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            CallableStatement statement = conn.prepareCall(callStoredProcedure);
            statement.setString(1, movie_title);
            statement.setInt(2, Integer.parseInt(movie_year));
            statement.setString(3, movie_director);
            statement.setString(4, star_name);
            if (star_birth_year != null && !star_birth_year.isEmpty()) {
                statement.setInt(5, Integer.parseInt(star_birth_year));
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            statement.setString(6, genre_name);
            statement.registerOutParameter(7, java.sql.Types.VARCHAR);
            statement.registerOutParameter(8, java.sql.Types.VARCHAR);
            statement.registerOutParameter(9, java.sql.Types.INTEGER);
            statement.executeUpdate();

            // Retrieve the generated star ID
            String movieId = statement.getString(7);
            String newStarId = statement.getString(8);
            int genreId = statement.getInt(9);

            // write a response object indicating success
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "Success!");
            responseJsonObject.addProperty("message", "Movie Id: " + movieId + ", Star Id: " + newStarId + ", Genre Id: " + genreId);
            request.getServletContext().log("Add Star Success");
            response.getWriter().write(responseJsonObject.toString());
        } catch (Exception e) {
            response.setStatus(500);
            e.printStackTrace();
        }
    }
}