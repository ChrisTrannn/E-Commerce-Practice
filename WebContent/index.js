/**
 * Handles the data returned by the API, reads the jsonObject, and populates data into HTML elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");
    console.log('hit');
    console.log(resultData);

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the HTML tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData[i]["title"] + "</td>";
        rowHTML += "<td>" + resultData[i]["year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["rating"] + "</td>";
        rowHTML += "<td>" + resultData[i]["genres"] + "</td>";
        rowHTML += "<td>" + resultData[i]["stars"] + "</td>";

        rowHTML += "</tr>";


        // Append the row created to the table body
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, the following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers the success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request URL, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});
