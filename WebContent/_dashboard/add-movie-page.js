let movie_form = $("#movie_form");

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
 * Handle the data returned by PaymentServlet
 * @param resultDataString jsonObject
 */
function handleMovieRequest(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle movie response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // show the response message
    $("#movie_status_message").text(resultDataJson["status"] + " " + resultDataJson["message"]);
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitMovieForm(formSubmitEvent) {
    console.log("submit add movie form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/add-movie", {
            method: "POST",
            // Serialize the payment form to the data sent by POST request
            data: movie_form.serialize(),
            success: handleMovieRequest
        }
    );
}

// Bind the submit action of the form to a handler function
movie_form.submit(submitMovieForm);