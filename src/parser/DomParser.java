package parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class DomParser {
    List<Movie> movies = new ArrayList<>();
    Set<Genre> genres = new HashSet<>();
    List<GenreAndMovie> genresInMovies = new ArrayList<>();
    List<StarAndMovie> starsInMovies = new ArrayList<>();
    List<Star> stars = new ArrayList<>();
    Set<String> processedFids = new HashSet<>();
    int movieBatchSize = 0;
    int starBatchSize = 0;
    int starMovieBatchSize = 0;
    int genreBatchSize = 0;
    int genreMovieBatchSize = 0;

    int inconsistencyCount = 0;
    int duplicateMovieCount = 0;
    int duplicateStarCount = 0;
    int starsNotFound = 0;
    int moviesNotFound = 0;

    Document moviesDom;
    Document castsDom;
    Document actorsDom;

    public void run() {
        // parse the xml file and get the dom object
        moviesDom = parseXmlFile("mains243.xml");
        castsDom = parseXmlFile("casts124.xml");
        actorsDom = parseXmlFile("actors63.xml");

        // get each employee element and create a Movie object
        parseDocument(moviesDom);
        parseCastsDocument(castsDom);
        parseActorsDocument(actorsDom);

        insertDataIntoDatabase();
        System.out.println("-------------------------------------------------------------------");
        System.out.println("Report:");
        System.out.println("Inserted " + movieBatchSize + " movies");
        System.out.println("Inserted " + starBatchSize + " stars");
        System.out.println("Inserted " + starMovieBatchSize + " stars_in_movies");
        System.out.println("Inserted " + genreBatchSize + " genres");
        System.out.println("Inserted " + genreMovieBatchSize + " genres_in_movies");
        System.out.println("Total inconsistencies: " + inconsistencyCount);
        System.out.println("Total duplicate movies: " + duplicateMovieCount);
        System.out.println("Total duplicate stars: " + duplicateStarCount);
        System.out.println("Total stars not found: " + starsNotFound);
        System.out.println("Total movies not found: " + moviesNotFound);
        System.out.println("-------------------------------------------------------------------");
    }

    private Document parseXmlFile(String filePath) {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // Ensuring that encoding of parser is ISO-8859-1
            FileInputStream fileInputStream = new FileInputStream("./src/parser/stanford-movies/" + filePath);
            InputSource inputSource = new InputSource(new InputStreamReader(fileInputStream, StandardCharsets.ISO_8859_1));

            // Parse using builder to get DOM representation of the XML file
            document = documentBuilder.parse(inputSource);

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
        return document;
    }

    private void parseCastsFile(String filePath) {
        parseXmlFile(filePath);
    }

    private void parseActorsFile(String filePath) {
        parseXmlFile(filePath);
    }


    private void parseDocument(Document dom) {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of directorfilm Elements, parse into movie object
        NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
        System.out.println(nodeList.getLength());

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element directorFilmsElement = (Element) nodeList.item(i);

            NodeList filmsList = directorFilmsElement.getElementsByTagName("films");

            for (int j = 0; j < filmsList.getLength(); j++) {
                Element filmsElement = (Element) filmsList.item(j);

                NodeList movieList = filmsElement.getElementsByTagName("film");

                for (int k = 0; k < movieList.getLength(); k++) {
                    Element movieElement = (Element) movieList.item(k);
                    String fid = getTextValue(movieElement, "fid");

                    if (!processedFids.contains(fid)) {
                        Movie movie = parseMovie(movieElement);
                        if (movie != null) {
//                            System.out.println(movie);
                            movies.add(movie);
                            processedFids.add(fid);  // Add fid to the set
                        }
                    } else {
//                        System.out.println("Skipping duplicate movie with fid: " + fid);
                        duplicateMovieCount++;
                    }
                }
            }
        }
        System.out.println("Total movies parsed: " + movies.size());
        System.out.println("Total genres parsed: " + genres.size());
        System.out.println("Total genres in movies parsed: " + genresInMovies.size());
        System.out.println(genres.toString());
    }

    private void parseCastsDocument(Document document) {
        Element documentElement = document.getDocumentElement();
        NodeList castList = documentElement.getElementsByTagName("m");

        for (int i = 0; i < castList.getLength(); i++) {
            Element castElement = (Element) castList.item(i);
            String filmId = getTextValue(castElement, "f");
            String stageName = getTextValue(castElement, "a");

            if (filmId != null && stageName != null && !stageName.equals("s a")) {
                stageName = cleanName(stageName);
                StarAndMovie starMovie = new StarAndMovie(stageName, filmId);
                starsInMovies.add(starMovie);
//                System.out.println("Star in Movie: " + filmId + ", " + stageName);
            } else {
//                System.out.println("Inconsistency found in cast: Element Name: " + castElement.getTagName() + "Node val: " + stageName);
                inconsistencyCount++;
            }
        }
        System.out.println("Total stars in movies parsed: " + starsInMovies.size());
    }

    private void parseActorsDocument(Document document) {
        Element documentElement = document.getDocumentElement();
        NodeList actorList = documentElement.getElementsByTagName("actor");

        for (int i = 0; i < actorList.getLength(); i++) {
            Element actorElement = (Element) actorList.item(i);
            String rawStageName = getTextValue(actorElement, "stagename");
            String stageName = cleanName(rawStageName);
            String birthYearStr = getTextValue(actorElement, "dob");
            Integer birthYear = isValidYear(birthYearStr) ? Integer.parseInt(birthYearStr) : null;

            if (stageName != null && !stageName.trim().isEmpty()) {
                Star star = findStarByName(stageName);
                if (star == null) {
                    // new star if not found
                    star = new Star(stageName, birthYear);
                    stars.add(star);
                } else {
                    System.out.println("Duplicate star found: " + stageName);
                    duplicateStarCount++;
                }
            } else {
                System.out.println("Inconsistency found in actor: Element Name: " + actorElement.getTagName() + "Node val: " + stageName);
                inconsistencyCount++;
            }
        }
        System.out.println("Total stars parsed: " + stars.size());
    }

    private boolean isValidYear(String year) {
        return year != null && year.matches("\\d{4}");
    }

    private String cleanName(String name) {
        // letters, digits, spaces, and common punctuation
        return name.replaceAll("[^a-zA-Z0-9\\s.,'-]", "");
    }

    private Star findStarByName(String Name) {
        for (Star star : stars) {
            if (star.getName().equals(Name)) {
                return star;
            }
        }
        return null;
    }

    private Movie parseMovie(Element element) {

        String id = getTextValue(element, "fid");
        if (id == null || id.trim().isEmpty()) {
            System.out.println("Skipping movie with no ID.");
            inconsistencyCount++;
            return null;
        }

        String title = getTextValue(element, "t");
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Skipping movie with no title, ID: " + id);
            inconsistencyCount++;
            return null;
        }

        int year = getIntValue(element, "year");
        if (year == -1) {
            System.out.println("Skipping movie with invalid year: " + title);
            inconsistencyCount++;
            return null;
        }

        String director = getTextValue(element, "dirn");
        if (director == null || director.trim().isEmpty() || director.startsWith("UnYear")) {
            System.out.println("Skipping movie with invalid director, ID: " + id);
            inconsistencyCount++;
            return null;
        }

        List<Genre> movieGenres = parseGenres(element);
        Movie movie = new Movie(id, title, year, director);
        movie.setGenres(movieGenres);

        for (Genre genre : movieGenres) {
            GenreAndMovie genreMovie = new GenreAndMovie(genre.getGenre(), id);
            System.out.println(genreMovie);
            genresInMovies.add(genreMovie);
            genres.add(genre);
        }

        return movie;
    }

    private List<Genre> parseGenres(Element filmElement) {
        List<Genre> genres = new ArrayList<>();
        NodeList genresList = filmElement.getElementsByTagName("cat");

        for (int i = 0; i < genresList.getLength(); i++) {
            Element genreElement = (Element) genresList.item(i);
            String genre = genreElement.getTextContent().trim();
            if (!genre.isEmpty()) {
                genre = GenreMapping.genreMapping.getOrDefault(genre, genre);

                genres.add(new Genre(genre));
            } else {
                System.out.println("Inconsistency found in genre: Element Name: " + genreElement.getTagName() + "Node val: " + genre);
                inconsistencyCount++;
            }
        }
        return genres;
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0 && nodeList.item(0).getFirstChild() != null) {
            textVal = nodeList.item(0).getTextContent().trim();
        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName) {
        try {
            return Integer.parseInt(getTextValue(ele, tagName));
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format for tag " + tagName + ": " + getTextValue(ele, tagName));
            return -1;  // -1 invalid year
        }
    }

    private void insertDataIntoDatabase() {
        Connection conn = null;
        String jdbcURL = "jdbc:mysql://localhost:3306/moviedb";
        PreparedStatement psInsertMovie = null;
        PreparedStatement psInsertStar = null;
        PreparedStatement psInsertStarMovie = null;
        PreparedStatement psInsertGenre = null;
        PreparedStatement psInsertGenreMovie = null;

        BufferedWriter movieWriter = null;
        BufferedWriter starWriter = null;
        BufferedWriter starMovieWriter = null;
        BufferedWriter genreWriter = null;
        BufferedWriter genreMovieWriter = null;

        String sqlInsertMovie = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
        String sqlInsertStar = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        String sqlInsertStarMovie = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
        String sqlInsertGenre = "INSERT INTO genres (name) VALUES (?)";
        String sqlInsertGenreMovie = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";

        Statement stmtFetchGenres = null;

        try {
            // Load the MySQL driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // Establish connection
            conn = DriverManager.getConnection(jdbcURL, "mytestuser", "My6$Password");
            conn.setAutoCommit(false); // Enable transaction management

            // Prepare statements
            psInsertMovie = conn.prepareStatement(sqlInsertMovie);
            psInsertStar = conn.prepareStatement(sqlInsertStar);
            psInsertStarMovie = conn.prepareStatement(sqlInsertStarMovie);
            psInsertGenre = conn.prepareStatement(sqlInsertGenre, Statement.RETURN_GENERATED_KEYS);
            psInsertGenreMovie = conn.prepareStatement(sqlInsertGenreMovie);
            stmtFetchGenres = conn.createStatement();


            /*
            movieWriter = new BufferedWriter(new FileWriter("movies_batch.log"));
            starWriter = new BufferedWriter(new FileWriter("stars_batch.log"));
            starMovieWriter = new BufferedWriter(new FileWriter("stars_in_movies_batch.log"));
            genreWriter = new BufferedWriter(new FileWriter("genres_batch.log"));
            genreMovieWriter = new BufferedWriter(new FileWriter("genres_in_movies_batch.log"));

*/

            // Fetch existing genres
            Set<String> existingGenres = new HashSet<>();
            Map<String, Integer> genreNameToIdMap = new HashMap<>();
            ResultSet rsGenres = stmtFetchGenres.executeQuery("SELECT id, name FROM genres");
            while (rsGenres.next()) {
                int id = rsGenres.getInt("id");
                String name = rsGenres.getString("name");
                genreNameToIdMap.put(name, id);
                existingGenres.add(rsGenres.getString("name"));
            }

            // Insert movies
            Set<String> insertedMovieIds = new HashSet<>();
            for (Movie movie : movies) {
                psInsertMovie.setString(1, movie.getId());
                psInsertMovie.setString(2, movie.getTitle());
                psInsertMovie.setInt(3, movie.getYear());
                psInsertMovie.setString(4, movie.getDirector());
                psInsertMovie.addBatch();
                //movieWriter.write("Inserting movie: " + movie.getId() + ", Title: " + movie.getTitle() + "\n");
                insertedMovieIds.add(movie.getId());
                movieBatchSize++;
            }

            psInsertMovie.executeBatch();
            System.out.println("Movies inserted. Batch size: " + movieBatchSize);

            // Insert stars
            Map<String, String> starNameToIdMap = new HashMap<>();
            for (Star star : stars) {
                String starId = generateUniqueId(star.getName());
                psInsertStar.setString(1, starId);
                psInsertStar.setString(2, star.getName());
                if (star.getDob() != null) {
                    psInsertStar.setInt(3, star.getDob());
                } else {
                    psInsertStar.setNull(3, Types.INTEGER);
                }
                //System.out.println("Inserting star: " + starId + ", Name: " + star.getName());
                //starWriter.write("Inserting star: " + starId + ", Name: " + star.getName() + "\n");
                psInsertStar.addBatch();
                starNameToIdMap.put(star.getName(), starId);
                starBatchSize++;
            }

            psInsertStar.executeBatch();
            System.out.println("Stars inserted. Batch size: " + starBatchSize);
            List<String> addedGenres = new ArrayList<>();

            // insert new genres
            for (Genre genre : genres) {
                if (!existingGenres.contains(genre.getGenre())) {
                    psInsertGenre.setString(1, genre.getGenre());
                    //genreWriter.write("Inserting genre: " + genre.getGenre() + "\n");
                    psInsertGenre.addBatch();
                    addedGenres.add(genre.getGenre());
                    genreBatchSize++;
                    existingGenres.add(genre.getGenre());
                } else {
                    System.out.println("Skipping existing genre: " + genre.getGenre());
                }
            }

            psInsertGenre.executeBatch();
            System.out.println("Genres inserted. Batch size: " + genreBatchSize);

            // get generated keys for new genres
            ResultSet generatedKeys = psInsertGenre.getGeneratedKeys();
            int index = 0;
            while (generatedKeys.next()) {
                int genreId = generatedKeys.getInt(1);
                String genreName = addedGenres.get(index);
                genreNameToIdMap.put(genreName, genreId);
                index++;
            }

            // Insert stars_in_movies
            Set<String> starMoviePairs = new HashSet<>();
            for (StarAndMovie starMovie : starsInMovies) {
                String starId = starNameToIdMap.get(starMovie.getStageName());
                String movieId = starMovie.getMovieId();
                String pair = starId + "-" + movieId;
                if (starId == null) {
                    System.out.println("Warning: No ID found for star: " + starMovie.getStageName());
                    //starMovieWriter.write("Warning: No ID found for star: " + starMovie.getStageName() + "\n");
                    starsNotFound++;
                } else if (insertedMovieIds.contains(movieId) && !starMoviePairs.contains(pair)) {
                    psInsertStarMovie.setString(1, starId);
                    psInsertStarMovie.setString(2, movieId);
//                    System.out.println("Linking star: " + starId + " to movie: " + movieId);
                    //starMovieWriter.write("Linking star: " + starId + " to movie: " + movieId + "\n");
                    psInsertStarMovie.addBatch();
                    starMoviePairs.add(pair);
                    starMovieBatchSize++;
                } else if (!insertedMovieIds.contains(movieId)) {
                    System.out.println("Skipping linking star to non-existent movie: " + movieId);
                    //starMovieWriter.write("Skipping linking star to non-existent movie: " + movieId + "\n");
                    moviesNotFound++;
                } else {
                    System.out.println("Duplicate star-movie pair: " + pair + " skipped.");
                    //starMovieWriter.write("Duplicate star-movie pair: " + pair + " skipped.\n");
                    duplicateMovieCount++;
                }
            }
            // Execute batches for relationships
            psInsertStarMovie.executeBatch();
            System.out.println("Stars in movies inserted. Batch size: " + starMovieBatchSize);

            // Insert genres_in_movies
            Set<String> genreMoviePairs = new HashSet<>();
            for (GenreAndMovie genreMovie : genresInMovies) {
                Integer genreId = genreNameToIdMap.get(genreMovie.getGenreId());
                String pair = genreId + "-" + genreMovie.getMovieId();
                if (genreId != null && !genreMoviePairs.contains(pair)) {
                    psInsertGenreMovie.setInt(1, genreId);
                    psInsertGenreMovie.setString(2, genreMovie.getMovieId());
                    //System.out.println("Linking genre ID: " + genreId + " to movie: " + genreMovie.getMovieId());
                            //genreMovieWriter.write("Linking genre ID: " + genreId + " to movie: " + genreMovie.getMovieId() + "\n");
                    psInsertGenreMovie.addBatch();
                    genreMoviePairs.add(pair);
                    genreMovieBatchSize++;
                }
            }

            psInsertGenreMovie.executeBatch();
            System.out.println("Genre in movies inserted. Batch size: " + genreMovieBatchSize);

            // Commit transactions
            conn.commit();

        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            try {
                if (psInsertMovie != null) psInsertMovie.close();
                if (psInsertStar != null) psInsertStar.close();
                if (psInsertStarMovie != null) psInsertStarMovie.close();
                if (psInsertGenre != null) psInsertGenre.close();
                if (psInsertGenreMovie != null) psInsertGenreMovie.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateUniqueId(String name) {
        // Generate hash and ensure it fits within VARCHAR(10)
        String hash = Integer.toHexString(name.hashCode());
        //System.out.println("Generated hash: " + hash + " Length: " + hash.length());
        return hash.length() > 8 ? hash.substring(0, 8) : String.format("%8s", hash).replace(' ', '0');
    }



        public static void main(String[] args) {
        DomParser actualParser = new DomParser();

        actualParser.run();
    }
}

