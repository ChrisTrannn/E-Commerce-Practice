let searchForm = $("#searchForm");

function handleSearchResult(resultDataString, formData) {
    console.log("Received search result:", resultDataString);

    if (resultDataString) {
        try {
            console.log("handle submission of search");

            // If search results are returned, redirect the user to movie-list.html with form data
            let queryString = $.param(formData);

            console.log("Redirecting to movie-list.html?" + queryString);

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



function submitSearch(formSubmitEvent) {
    console.log("submit login form");

    formSubmitEvent.preventDefault();

    let formData = {
        title: $('#searchForm input[name="title"]').val(),
        year: $('#searchForm input[name="year"]').val(),
        director: $('#searchForm input[name="director"]').val(),
        starName: $('#searchForm input[name="starName"]').val()
    };

    // Convert the form data to a query string
    let queryString = $.param(formData);
    $.ajax({
        url: "api/movies",
        method: "GET",
        data: queryString,
        success: function(resultDataString) {
            handleSearchResult(resultDataString, formData);
        }
    });
}

// Bind the submit action of the form to a handler function
searchForm.submit(submitSearch);

$("#searchToast").on("hidden.bs.toast", function () {
    // Hide the toast
    $("#searchToast").toast("hide");
});