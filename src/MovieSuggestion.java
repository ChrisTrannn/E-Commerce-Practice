import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "MovieSuggestionServlet", urlPatterns = "/api/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /*
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String queryParam = request.getParameter("query");
        String[] keywords = queryParam.split("\\s+");
        StringBuilder searchTerms = new StringBuilder();

        for (String keyword : keywords) {
            searchTerms.append("+").append(keyword).append("* ").toString();
        }

        System.out.println(queryParam);
        if (queryParam == null || queryParam.trim().isEmpty()) {
            response.getWriter().write("[]");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE) LIMIT 10;";
            System.out.println(query);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, searchTerms.toString().trim());

            ResultSet resultSet = statement.executeQuery();
            System.out.println(resultSet);
            JsonArray jsonArray = new JsonArray();
            while (resultSet.next()) {
                String movieID = resultSet.getString("id");
                String title = resultSet.getString("title");
                System.out.println("movieID: " + movieID + ", title: " + title);
                JsonObject jsonObject = generateJsonObject(movieID, title);
                System.out.println(jsonObject);
                jsonArray.add(jsonObject);
            }
            response.getWriter().write(jsonArray.toString());
        } catch (SQLException sqle) {
            System.err.println("SQL Error: " + sqle.getMessage());
            sqle.printStackTrace();
        } catch (Exception e) {
            System.err.println("General Error: " + e.getMessage());
            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.toString());
            response.getWriter().write(jsonObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // search on superheroes and add the results to JSON Array
        // this example only does a substring match
        // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars


    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieID, String movieTitle) {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("value", movieTitle);

            JsonObject additionalDataJsonObject = new JsonObject();
            additionalDataJsonObject.addProperty("movieID", movieID);

            jsonObject.add("data", additionalDataJsonObject);
            return jsonObject;
        } catch (Exception e) {
            System.err.println("Error generating JSON object: " + e.getMessage());
            throw e;
        }
    }



}
