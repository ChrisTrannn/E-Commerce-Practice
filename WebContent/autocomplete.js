/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    // TODO: if you want to check past query results first, you can do it here

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    if (query.length >= 3) {
        // key to use in sessionStorage
        var storageKey = 'autocomplete_' + encodeURIComponent(query);

        // attempting to retrieve the cached suggestions
        var cachedSuggestions = sessionStorage.getItem(storageKey);

        if (cachedSuggestions) {
            console.log("Using cached results for query:", query);
            // Call doneCallback with the cached suggestions parsed from the JSON string
            doneCallback({suggestions: JSON.parse(cachedSuggestions)});
        } else {
            console.log("No cached results, sending AJAX request for query:", query);
            jQuery.ajax({
                "method": "GET",
                // generate the request url from the query.
                // escape the query string to avoid errors caused by special characters
                "url": "api/movie-suggestion?query=" + query,
                "success": function (data) {
                    console.log("Caching new results for query:", query);
                    // Cache new suggestions in sessionStorage
                    sessionStorage.setItem(storageKey, JSON.stringify(data));
                    // pass the data, query, and doneCallback function into the success handler
                    handleLookupAjaxSuccess(data, query, doneCallback);
                },
                "error": function (errorData) {
                    console.log("lookup ajax error")
                    console.log(errorData)
                }
            })
        }
    } else {
        console.log("Query too short for autocomplete");
        // Optionally clear any existing suggestions if the query is too short
        doneCallback({ suggestions: [] });
    }
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    //var jsonData = JSON.parse(data);
    console.log(data)

    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    var movieId = suggestion["data"]["movieID"];
    var currentUrl = window.location.href;

    var newUrl = currentUrl.replace(/\/[^\/]*$/, "/single-movie.html?id="+movieId);

    // jump to the specific result page based on the selected suggestion
    window.location.href = newUrl;
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time 300 ms
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearchResult(resultDataString, formData) {
    console.log("Received search result:", resultDataString);

    if (resultDataString) {
        try {
            console.log("handle submission of search");

            // If search results are returned, redirect the user to movie-list.html with form data
            let queryString = $.param(formData);

            window.location.href = "movie-list.html?" + queryString;
        } catch (error) {
            console.error("Error parsing JSON:", error);
        }
    } else {
        console.log("No search results found");
        $("#searchToastBody").text("No search results found.");
        $("#searchToast").toast("show");

    }
}

function handleNormalSearch(query) {
    console.log("doing normal full text search with query: " + query);

    let formData = {
        title: $('#autocomplete').val()
    };

    $.ajax({
        url: "api/movies",
        method: "GET",
        data: formData,
        success: function(resultDataString) {
            handleNormalSearchResult(resultDataString, formData);
        }
    });
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})