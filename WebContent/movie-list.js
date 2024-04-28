/**
 * Handles the data returned by the API, reads the jsonObject, and populates data into HTML elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Retrieve the current value of perPage from the dropdown
    var perPage = parseInt($('#perPageSelect').val()) || 10;

    // Populate the movie table, Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    console.log(resultData);

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        var price = (Math.random() * (25 - 5) + 5).toFixed(2);
        // Concatenate the HTML tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        // Add a link to single-movie.html with id passed with GET url parameter
        rowHTML += "<td>" + '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">' + resultData[i]["title"] + '</a>' + "</td>";

        rowHTML += "<td>" + resultData[i]["year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["genres"] + "</td>";

        // Add a link to single-star.html with id passed with GET url parameter
        rowHTML += "<td>";
        for (let j = 0; j < Math.min(3, resultData[i]["stars"].length); j++) {
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

    if (resultData.length < perPage) {
        console.log(resultData.length)
        console.log(perPage)
        $('#nextButton').prop('disabled', true);
    } else {
        $('#nextButton').prop('disabled', false);
    }
}

// Function to parse query parameters from URL
function parseQueryString() {
    var queryString = window.location.search.substring(1);
    var params = {};
    var pairs = queryString.split("&");
    for (var i = 0; i < pairs.length; i++) {
        var pair = pairs[i].split("=");
        params[pair[0]] = decodeURIComponent(pair[1]);
    }
    return params;
}

// Function to make AJAX call with query parameters
function searchMovies(queryParams) {
    $.ajax({
        url: "api/movies",
        method: "GET",
        data: queryParams,
        success: handleMovieResult,
        error: function(xhr, status, error) {
            console.error("Error fetching movies:", error);
        }
    });
}
// Function to handle adding items to the cart
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

    // Submit the cart data to the server
    $.ajax({
        url: "api/shopping-cart",
        method: "POST",
        data: cartData,
        success: function(resultDataString) {
            // Parse the JSON response
            console.log(resultDataString);
            console.log("Item added to cart:", resultDataString);
            alert('"' + title + '" added to cart!');
        },
        error: function(xhr, status, error) {
            console.error("Error adding item to cart:", error);
        }
    });
}


$(document).ready(function() {    // Function to handle adding items to the cart
    $("#movie_table_body").on("click", ".addToCartButton", addToCart);
    // Get the current page URL
    var currentPageURL = window.location.href;

    console.log(currentPageURL);

    // Store the current page URL in sessionStorage as the previous URL
    sessionStorage.setItem('previousPageURL', currentPageURL);
    console.log("Document is ready.");
    var queryParams = parseQueryString();

    // Retrieve stored page number from sessionStorage
    let storedPageNum = sessionStorage.getItem('selectedPageNum');
    if (storedPageNum) {
        $('#pageNum').text(storedPageNum);
    }

    searchMovies(queryParams);

// Event listener for "Next" button
    $('#nextButton').click(function() {
        var pageNum = parseInt($('#pageNum').text()) + 1;
        var perPage = parseInt($('#perPageSelect').val()) || 10;
        handlePagination(pageNum, perPage);
    });

    $('#prevButton').click(function() {
        var pageNum = parseInt($('#pageNum').text()) - 1;
        if (pageNum < 1) pageNum = 1; // Ensure pageNum doesn't go below 1
        var perPage = parseInt($('#perPageSelect').val()) || 10;
        handlePagination(pageNum, perPage);
    });

    $('#perPageSelect').change(function() {
        var pageNum = parseInt($('#pageNum').text());
        var perPage = parseInt($(this).val());

        let queryParams = parseQueryString();
        queryParams['pageNum'] = pageNum;
        queryParams['perPage'] = perPage;

        // Store the selected perPage value in session storage
        sessionStorage.setItem('selectedPerPage', perPage);

        // Redirect to the new URL with the updated query parameters
        let url = window.location.pathname + '?' + $.param(queryParams);
        window.location.href = url;
    });

    // Set the selected perPage value from session storage if available
    let storedPerPage = sessionStorage.getItem('selectedPerPage');
    if (storedPerPage) {
        $('#perPageSelect').val(storedPerPage);
    }


    let storedSortOption = sessionStorage.getItem('selectedSortOption');
    if (storedSortOption && storedSortOption.trim() !== "Sort Movies") {
        $('#sortingSelect option').filter(function() {
            return $(this).text() === storedSortOption;
        }).prop('selected', true);
        $('#sortingSelect').next('.dropdown-toggle').text(storedSortOption);
    }

    var currentPageNum = parseInt($('#pageNum').text());
    if (currentPageNum === 1) {
        $('#prevButton').prop('disabled', true);
    } else {
        $('#prevButton').prop('disabled', false);
    }


    // Event listener for sorting select
    $('#sortingSelect').change(function() {
        let sortingParam = $(this).val();
        let selectedSortOption = $(this).find('option:selected').text();

        // Store the selected sorting option in session storage
        sessionStorage.setItem('selectedSortOption', selectedSortOption);

        // Update the sorting parameter in the query parameters
        let queryParams = parseQueryString();
        queryParams['sort'] = sortingParam;

        // Redirect to the new URL with the updated query parameters
        let url = window.location.pathname + '?' + $.param(queryParams);
        window.location.href = url;
    });

});

// Function to handle pagination
function handlePagination(pageNum = 1, perPage = 10) {
    var queryParams = parseQueryString();
    console.log(pageNum, perPage)
    queryParams['pageNum'] = pageNum;
    queryParams['perPage'] = perPage;

    // Store the current page number in sessionStorage
    sessionStorage.setItem('selectedPageNum', pageNum);

    // Update the page number text
    $('#pageNum').text(pageNum);

    var url = window.location.pathname + '?' + $.param(queryParams);

    window.location.href = url;

    searchMovies(queryParams);
}

