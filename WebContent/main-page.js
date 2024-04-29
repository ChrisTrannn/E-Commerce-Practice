// populates the genres list on the main page with results from the genres API
function handleGenresResult(resultData) {
    console.log("handleGenresResult: populating genres");

    // grab the genres list element
    let genresListElement = jQuery("#genres_list");

    // iterate through the resultData and add <li> elements to the genresListElement
    for (let i = 0; i < resultData.length; i++) {
        let genre = resultData[i]["name"];
        let id = resultData[i]["id"];
        let li = `<li><a href="movie-list.html?genre_id=${id}">${genre}</a></li>`;
        genresListElement.append(li);
    }
}

// Makes the HTTP GET request to genres URL and registers the success callback function handleGenresResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/genre",
    success: (resultData) => handleGenresResult(resultData)
});

// grab the titles_list_characters element
let titlesListCharactersElement = jQuery("#titles_list_characters");
// iterate through A-Z and add elements
for (let i = 65; i <= 90; i++) {
    let letter = String.fromCharCode(i);
    let a = `<a href="movie-list.html?title_id=${letter}">${letter}</a>`;
    titlesListCharactersElement.append(a);
}

// grab the titles_list_numbers element
let titlesListNumbersElement = jQuery("#titles_list_numbers");
// iterate through 0-9 and add elements
for (let i = 0; i <= 9; i++) {
    let a = `<a href="movie-list.html?title_id=${i}">${i}</a>`;
    titlesListNumbersElement.append(a);
}
titlesListNumbersElement.append(`<a href="movie-list.html?title_id=*">*</a>`);