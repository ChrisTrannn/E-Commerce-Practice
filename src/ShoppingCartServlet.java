import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the session
        HttpSession session = request.getSession();

        // Get the cart items from the session
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");

        // If there are no previous items, create a new ArrayList
        if (previousItems == null) {
            previousItems = new ArrayList<>();
        }

        // Construct a JSON response with the cart items
        JsonObject responseJsonObject = new JsonObject();
        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        System.out.println(responseJsonObject);
        // Set response content type and write JSON response
        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the movie ID from the request parameter
        String movieId = request.getParameter("movieId");
        int quantityIncrement = Integer.parseInt(request.getParameter("quantityIncrement"));
        double price = Double.parseDouble(request.getParameter("price"));
        String title = request.getParameter("title");

        // Get the session
        HttpSession session = request.getSession();

        // Get the cart items from the session
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");

        // If there are no previous items, create a new ArrayList
        if (previousItems == null) {
            previousItems = new ArrayList<>();
        }

        // Check if the item already exists in the cart
        boolean itemExists = false;
        int newQuantity = 0;
        double totalPrice = 0;
        for (String item : previousItems) {
            JsonObject itemJson = new Gson().fromJson(item, JsonObject.class);
            String existingMovieId = itemJson.get("movieId").getAsString();
            if (existingMovieId.equals(movieId)) {
                // If the item exists, update its quantity
                int existingQuantity = itemJson.get("quantity").getAsInt();
                System.out.println(existingQuantity);
                newQuantity = existingQuantity + quantityIncrement;
                itemJson.addProperty("quantity", newQuantity);
                totalPrice = price * newQuantity;
                itemJson.addProperty("totalPrice", totalPrice);
                itemExists = true;
                break;
            }
        }

        // If the item doesn't exist, add it as a new item with the incremented quantity
        if (!itemExists) {
            JsonObject newItem = new JsonObject();
            newItem.addProperty("movieId", movieId);
            newItem.addProperty("quantity", quantityIncrement);
            newItem.addProperty("price", price);
            newItem.addProperty("title", title);
            totalPrice = price * quantityIncrement;
            newItem.addProperty("totalPrice", totalPrice);
            previousItems.add(newItem.toString());
            newQuantity = quantityIncrement;
        } else {
            // Update the existing item in the list with the new quantity
            for (int i = 0; i < previousItems.size(); i++) {
                JsonObject itemJson = new Gson().fromJson(previousItems.get(i), JsonObject.class);
                String existingMovieId = itemJson.get("movieId").getAsString();
                if (existingMovieId.equals(movieId)) {
                    itemJson.addProperty("quantity", newQuantity);
                    previousItems.set(i, itemJson.toString());
                    break;
                }
            }
        }

        // Store the updated cart items back to the session
        session.setAttribute("previousItems", previousItems);

        // Convert previousItems to JsonArray
        JsonArray previousItemsArray = new JsonArray();
        for (String item : previousItems) {
            previousItemsArray.add(new Gson().fromJson(item, JsonObject.class));
        }

        // Construct a JSON response indicating success
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("status", "success");
        responseJsonObject.addProperty("message", "Quantity updated successfully");
        responseJsonObject.addProperty("newQuantity", newQuantity);
        responseJsonObject.addProperty("totalPrice", totalPrice);
        responseJsonObject.add("previousItems", previousItemsArray);

        // Set response content type and write JSON response
        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the movie ID to delete from the request parameter
        String movieIdToDelete = request.getParameter("movieId");

        // Get the session
        HttpSession session = request.getSession();

        // Get the cart items from the session
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");

        if (previousItems != null) {
            // Iterate over cart items and remove the item with the matching movieId
            for (int i = 0; i < previousItems.size(); i++) {
                JsonObject itemJson = new Gson().fromJson(previousItems.get(i), JsonObject.class);
                String existingMovieId = itemJson.get("movieId").getAsString();
                if (existingMovieId.equals(movieIdToDelete)) {
                    previousItems.remove(i); // Remove the item from the cart items list
                    break; // Exit the loop after removing the item
                }
            }

            // Update the cart items in the session
            session.setAttribute("previousItems", previousItems);
        }

        // Construct a JSON response indicating success
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("status", "success");
        responseJsonObject.addProperty("message", "Item deleted successfully");

        // Set response content type and write JSON response
        response.setContentType("application/json");
        response.getWriter().write(responseJsonObject.toString());
    }



}