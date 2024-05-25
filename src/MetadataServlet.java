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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

// Declaring a WebServlet called MetadataServlet, which maps to url "/_dashboard/api/metadata"
@WebServlet(name = "MetadataServlet", urlPatterns = "/_dashboard/api/metadata")
public class MetadataServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        dataSource = getRandomDataSource();

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT \n" +
                            "    table_name, \n" +
                            "    GROUP_CONCAT(CONCAT(column_name, ':', data_type) ORDER BY ordinal_position SEPARATOR ',') AS columns_info\n" +
                            "FROM \n" +
                            "    information_schema.columns\n" +
                            "WHERE \n" +
                            "    table_schema = 'moviedb'\n" +
                            "GROUP BY \n" +
                            "    table_name;\n";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            // Create a JsonArray to hold the data we retrieve from rs
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String table_name = rs.getString("table_name");
                String columns_info = rs.getString("columns_info");

                // create json array called columns and add a json object with two values: attribute and type
                JsonArray columns = new JsonArray();
                for (String column : columns_info.split(",")) {
                    String[] column_info = column.split(":");
                    JsonObject columnObj = new JsonObject();
                    columnObj.addProperty("attribute", column_info[0]);
                    columnObj.addProperty("type", column_info[1]);
                    columns.add(columnObj);
                }

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("table_name", table_name);
                jsonObject.add("columns", columns);

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
    }
}
