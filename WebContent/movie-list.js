/**
 * Handles the data returned by the API, reads the jsonObject, and populates data into HTML elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    console.log(resultData)

    // Populate the movie table, Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
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
        rowHTML += "</tr>";

        // Append the row created to the table body
        movieTableBodyElement.append(rowHTML);
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
function searchMovies(formData) {
    $.ajax({
        url: "api/search",
        method: "GET",
        data: formData,
        success: handleMovieResult,
        error: function(xhr, status, error) {
            console.error("Error fetching movies:", error);
        }
    });
}

$(document).ready(function() {
    var queryParams = parseQueryString();

    searchMovies(queryParams);
});