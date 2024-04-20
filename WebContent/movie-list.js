// parses the url and get the parameter of genre or title
function getUrlParameter(name) {
    name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    let regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
    let results = regex.exec(location.search);
    return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
}

// Handles the data returned by the API, reads the jsonObject, and populates data into HTML elements
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

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

// Get the parameter from url, parameter could be title or genre
let title = getUrlParameter("title");
let genre = getUrlParameter("genre");

// make GET request to get the movie list: if title, url "api/movies?title=" + title; if genre, url "api/movies?genre=" + genre
if (title !== "") {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies?title=" + title,
        success: (resultData) => handleMovieResult(resultData)
    });
} else if (genre !== "") {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies?genre=" + genre,
        success: (resultData) => handleMovieResult(resultData)
    });
} else {
    // Makes the HTTP GET request and registers the success callback function handleMovieResult
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movies", // Setting request URL, which is mapped by MoviesServlet in Movies.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
    });
}