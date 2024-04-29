function handleResult(resultData) {
    console.log("handleResult: populating confirmation page from resultData")
    // populate the movie info h3, find the empty h3 body by id "movie_info"
    let confirmationTableBody = jQuery("#confirmation_table_body");

    let totalPrice = 0;
    // Iterate through resultData, looks like {movies: [{saleId, movieTitle, quantity, price, totalPrice}]}
    for (let i = 0; i < resultData['movies'].length; i++) {
        // Concatenate the HTML tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData['movies'][i]["saleId"] + "</td>";
        rowHTML += "<td>" + resultData['movies'][i]["movieTitle"] + "</td>";
        rowHTML += "<td>" + resultData['movies'][i]["quantity"] + "</td>";
        rowHTML += "<td>" + resultData['movies'][i]["price"] + "</td>";
        rowHTML += "<td>" + resultData['movies'][i]["totalPrice"] + "</td>";
        rowHTML += "</tr>";
        totalPrice += parseFloat(resultData['movies'][i]["totalPrice"]);

        // Append the row created to the table body
        confirmationTableBody.append(rowHTML);
    }

    // change the text here #confirmation_total_price
    let confirmationTotalPrice = jQuery("#confirmation_total_price");
    confirmationTotalPrice.append(totalPrice);
}

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/payment",
    success: (resultData) => handleResult(resultData)
});