document.addEventListener("DOMContentLoaded", function() {
    // Fetch cart items from server and display them
    fetchCartItems();

    // Add event listener to proceed to payment button
    document.getElementById("proceedToPaymentBtn").addEventListener("click", function() {
        // Redirect to payment page
        window.location.href = "payment.html";
    });
});

function fetchCartItems() {
    $.ajax({
        url: "api/shopping-cart",
        method: "GET",
        dataType: "json",
        success: function(cartItems) {
            console.log(cartItems);
            displayCartItems(cartItems);
        },
        error: function(xhr, status, error) {
            console.error("Error fetching cart items:", error);
        }
    });
}


function displayCartItems(cartItems) {
    const cartItemsDiv = document.getElementById("cartItems");
    cartItemsDiv.innerHTML = ""; // Clear previous items
    let total = 0;
    // Check if cartItems['previousItems'] is an array
    if (Array.isArray(cartItems['previousItems'])) {
        // Iterate over each item in the array
        cartItems['previousItems'].forEach(item => {
            const parsedItem = JSON.parse(item);
            // Create a div to hold each cart item
            const itemDiv = document.createElement("div");
            itemDiv.className = "cart-item";

            itemDiv.style.marginBottom = "20px";

            // Display title
            const titleDiv = document.createElement("h4");
            titleDiv.textContent = `Title: ${parsedItem.title}`;
            itemDiv.appendChild(titleDiv);

            // Display quantity with plus and minus buttons
            const quantityDiv = document.createElement("div");
            quantityDiv.textContent = "Quantity: ";
            const minusButton = document.createElement("button");

            minusButton.textContent = "-";
            minusButton.addEventListener("click", function() {
                parsedItem.quantity--;
                quantityDisplay.textContent = parsedItem.quantity;
                totalPriceDiv.textContent = `Total Price: ${parsedItem.price * parsedItem.quantity}`;
                decrementQuantity(parsedItem.movieId, parsedItem.title, parsedItem.price, parsedItem.quantity);
                updateTotal();
            });
            quantityDiv.appendChild(minusButton);
            const quantityDisplay = document.createElement("span");
            quantityDisplay.className = "quantity";

            quantityDisplay.textContent = parsedItem.quantity;
            quantityDiv.appendChild(quantityDisplay);
            const plusButton = document.createElement("button");
            plusButton.textContent = "+";
            plusButton.addEventListener("click", function() {
                parsedItem.quantity++;
                quantityDisplay.textContent = parsedItem.quantity;
                totalPriceDiv.textContent = `Total Price: ${parsedItem.price * parsedItem.quantity}`;
                incrementQuantity(parsedItem.movieId, parsedItem.title, parsedItem.price, parsedItem.quantity);
                updateTotal();
            });
            quantityDiv.appendChild(plusButton);
            itemDiv.appendChild(quantityDiv);

            // Display delete option
            const deleteButton = document.createElement("button");
            deleteButton.textContent = "Delete";
            deleteButton.addEventListener("click", function() {
                itemDiv.remove();
                // Call a function to delete the item from the cart
                deleteCartItem(parsedItem.movieId);
                updateTotal();
            });
            itemDiv.appendChild(deleteButton);

            // Display price of each movie
            const priceDiv = document.createElement("div");
            priceDiv.textContent = `Price: ${parsedItem.price}`;
            itemDiv.appendChild(priceDiv);

            // Display total price (price * quantity)
            const totalPriceDiv = document.createElement("div");
            totalPriceDiv.textContent = `Total Price: ${parsedItem.price * parsedItem.quantity}`;
            itemDiv.appendChild(totalPriceDiv);

            // Append the cart item div to the cart items container
            cartItemsDiv.appendChild(itemDiv);

            // Add item's total price to the total
            total += parsedItem.price * parsedItem.quantity;
        });
    } else {
        console.error("Invalid cart items format:", cartItems);
    }
    // Display total outside the loop
    const totalDiv = document.createElement("div");
    totalDiv.textContent = `Checkout Total: ${(total)}`;
    document.querySelector(".total").appendChild(totalDiv);

    // Call updateTotal to ensure initial calculation is displayed
    updateTotal();
}

// Function to update total
function updateTotal() {
    let total = 0;
    const totalPriceDivs = document.querySelectorAll(".cart-item > div:last-child");
    totalPriceDivs.forEach(div => {
        const price = parseFloat(div.textContent.split(":")[1].trim());
        total += price;
    });
    const totalDiv = document.querySelector(".total");
    totalDiv.textContent = `Checkout Total: ${total}`;
}

// Function to increment quantity
function incrementQuantity(movieId, title, price, quantity) {
    // Create the cart data object
    var cartData = {
        movieId: movieId,
        title: title,
        price: price,
        quantity: quantity,
        quantityIncrement: 1
    };

    // Submit the cart data to the server
    $.ajax({
        url: "api/shopping-cart", // Specify the endpoint for incrementing quantity
        method: "POST",
        data: cartData,
        success: function(resultDataString) {
            // Handle the response, if needed
            console.log("Quantity incremented:", resultDataString);
            console.log(resultDataString['newQuantity']);

            // Update the displayed quantity, if needed
            const quantityDisplay = document.querySelector(`.cart-item[data-movie-id="${movieId}"] span.quantity`);
            console.log(quantityDisplay);
            if (quantityDisplay) {
                console.log(quantityDisplay.textContent);
                quantityDisplay.textContent = resultDataString['newQuantity'];
                console.log(quantityDisplay.textContent);
            }
        },
        error: function(xhr, status, error) {
            console.error("Error incrementing quantity:", error);
        }
    });
}


function decrementQuantity(movieId, title, price, quantity) {
    // Create the cart data object
    var cartData = {
        movieId: movieId,
        title: title,
        price: price,
        quantity: quantity,
        quantityIncrement: -1 // Use negative quantityIncrement for decrementing
    };

    // Submit the cart data to the server
    $.ajax({
        url: "api/shopping-cart", // Specify the endpoint for decrementing quantity
        method: "POST",
        data: cartData,
        success: function(resultDataString) {
            // Handle the response, if needed
            console.log("Quantity decremented:", resultDataString);
            console.log(resultDataString['newQuantity']);

            // Update the displayed quantity, if needed
            const quantityDisplay = document.querySelector(`.cart-item[data-movie-id="${movieId}"] span.quantity`);
            if (quantityDisplay) {
                quantityDisplay.textContent = resultDataString['newQuantity'];
            }
        },
        error: function(xhr, status, error) {
            console.error("Error decrementing quantity:", error);
        }
    });
}


function deleteCartItem(movieId) {
    // Make a DELETE request to delete the item from the cart
    $.ajax({
        url: "api/shopping-cart", // Endpoint for deleting cart items
        method: "DELETE",
        data: {movieId: movieId}, // Include the movieId to identify which item to delete
        success: function (response) {
            console.log("Item deleted successfully:", response);
            // Handle any UI updates or notifications here
        },
        error: function (xhr, status, error) {
            console.error("Error deleting item from cart:", error);
            // Handle any error messages or UI updates here
        }
    });

}
