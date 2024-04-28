let payment_form = $("#payment_form");

/**
 * Handle the data returned by PaymentServlet
 * @param resultDataString jsonObject
 */
function handlePaymentRequest(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If payment succeeds, it will redirect the user to confirmation-page.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("confirmation-page.html");
    } else {
        // If payment fails, the web page will display
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#payment_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the payment form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentRequest
        }
    );
}

// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);