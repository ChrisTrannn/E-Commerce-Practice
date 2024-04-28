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
                totalPriceDiv.textContent = `Total Price: ${(parsedItem.price * parsedItem.quantity).toFixed(2)}`;
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
                totalPriceDiv.textContent = `Total Price: ${(parsedItem.price * parsedItem.quantity).toFixed(2)}`;
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
            priceDiv.textContent = `Price: ${parsedItem.price.toFixed(2)}`;
            itemDiv.appendChild(priceDiv);

            // Display total price (price * quantity)
            const totalPriceDiv = document.createElement("div");
            totalPriceDiv.textContent = `Total Price: ${(parsedItem.price * parsedItem.quantity).toFixed(2)}`;
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
    totalDiv.textContent = `Checkout Total: ${total.toFixed(2)}`;
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
    totalDiv.textContent = `Checkout Total: $${total.toFixed(2)}`;
}

// Function to increment quantity
function incrementQuantity(movieId, title, price, quantity) {
    var cartData = {
        movieId: movieId,
        title: title,
        price: price,
        quantity: quantity,
        quantityIncrement: 1
    };

    // Submit the cart data to the server
    $.ajax({
        url: "api/shopping-cart",
        method: "POST",
        data: cartData,
        success: function(resultDataString) {
            const quantityDisplay = document.querySelector(`.cart-item[data-movie-id="${movieId}"] span.quantity`);
            console.log(quantityDisplay);
            if (quantityDisplay) {
                quantityDisplay.textContent = resultDataString['newQuantity'];
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
        quantityIncrement: -1
    };

    $.ajax({
        url: "api/shopping-cart",
        method: "POST",
        data: cartData,
        success: function(resultDataString) {
            console.log("Quantity decremented:", resultDataString);
            console.log(resultDataString['newQuantity']);

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
    $.ajax({
        url: "api/shopping-cart",
        method: "DELETE",
        data: {movieId: movieId},
        success: function (response) {
            console.log("Item deleted successfully:", response);
        },
        error: function (xhr, status, error) {
            console.error("Error deleting item from cart:", error);
        }
    });

}
