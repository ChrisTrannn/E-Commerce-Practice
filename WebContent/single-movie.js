/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");
    // change the title of the page to the movie name
    let movieTitle = jQuery("#movie_title");
    movieTitle.append(resultData[0]["title"]);

    // populate the movie info h3, find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    // append movie title and birth year via template string
    movieInfoElement.append(`<p>${resultData[0]["title"]} (${resultData[0]["year"]})</p>`);

    console.log("handleResult: populating movie table from resultData");

    // Populate the movie table, Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the HTML tags with resultData jsonObject
        var price = (Math.random() * (25 - 5) + 5).toFixed(2);
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData[i]["title"] + "</td>";
        rowHTML += "<td>" + resultData[i]["year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["genres"] + "</td>";

        // Add a link to single-star.html with id passed with GET url parameter
        rowHTML += "<td>";
        for (let j = 0; j < resultData[i]["stars"].length; j++) {
            rowHTML += '<a href="single-star.html?id=' + resultData[i]["stars"][j]["id"] + '">' +
                resultData[i]["stars"][j]["name"] + ', ' +
                '</a>';
        }
        rowHTML += "</td>";
        rowHTML += "<td>" + resultData[i]["rating"] + "</td>";
        rowHTML += '<td><button class="addToCartButton" data-movie-id="' + resultData[i]["movie_id"] + '" data-title="' + resultData[i]["title"] + '" data-price="' + price + '">Add to Cart</button></td>';
        rowHTML += "</tr>";

        // Append the row created to the table body
        movieTableBodyElement.append(rowHTML);
    }
}

function addToCart(event) {
    // Prevent the default form submission behavior
    event.preventDefault();

    // Retrieve the movie ID from the clicked button's data attribute
    var movieId = $(event.currentTarget).data("movie-id");
    var title = $(event.currentTarget).data("title");
    var price = parseFloat($(event.currentTarget).data("price"));

    // Create the cart data object
    var cartData = {
        movieId: movieId,
        title: title,
        price: price,
        quantity: 0,
        quantityIncrement: 1
    };

    console.log(cartData);

    $.ajax({
        url: "api/shopping-cart",
        method: "POST",
        data: cartData,
        success: function(resultDataString) {
            // Parse the JSON response
            console.log(resultDataString);
            //var resultDataJson = JSON.parse(resultDataString);

            console.log("Item added to cart:", resultDataString);
        },
        error: function(xhr, status, error) {
            console.error("Error adding item to cart:", error);
        }
    });
}

$(document).ready(function() {
    $(document).on("click", ".addToCartButton", addToCart);
});


$(function() {
    // Get the previous page URL from sessionStorage
    var previousPageURL = sessionStorage.getItem('previousPageURL');
    console.log(previousPageURL);

    $('#prevButtonMovie').click(function() {
        console.log('Button clicked');
        window.location.href = previousPageURL;
    });
});

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by SingleMovieServlet
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});