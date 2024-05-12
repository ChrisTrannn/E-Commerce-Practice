// populates the metadata list on the main page with results from the metadata API
function handleMetadataResult(resultData) {
    console.log("handleMetadataResult: populating genres");
    console.log(resultData);

    // create tables with the metadata and append to the metadata_tables div
    let metadata_tables = jQuery("#metadata_tables");

    for (let i = 0; i < resultData.length; i++) {
        let table = resultData[i];
        let table_name = table["table_name"];
        let columns = table["columns"];

        // HTML to be appended to the metadata_tables div
        let tableHTML = "<table class='table table-striped table-bordered table-hover'>";
        tableHTML += "<thead><tr><th>Attribute</th><th>Type</th></tr></thead>";
        tableHTML += "<tbody>";

        // column data for each table
        for (let j = 0; j < columns.length; j++) {
            let column = columns[j];
            let attribute = column["attribute"];
            let type = column["type"];

            tableHTML += "<tr>";
            tableHTML += "<td>" + attribute + "</td>";
            tableHTML += "<td>" + type + "</td>";
            tableHTML += "</tr>";
        }

        tableHTML += "</tbody></table>";

        // append the table to the metadata_tables div
        metadata_tables.append("<h3>" + table_name + "</h3>");
        metadata_tables.append(tableHTML);
    }
}

// Makes the HTTP GET request to metadata URL and registers the success callback function handleMetadataResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/metadata",
    success: (resultData) => handleMetadataResult(resultData)
});