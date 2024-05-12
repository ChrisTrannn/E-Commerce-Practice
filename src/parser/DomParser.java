package parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class DomParser {
    List<Movie> movies = new ArrayList<>();
    Set<Genre> genres = new HashSet<>();
    List<GenreAndMovie> genresInMovies = new ArrayList<>();
    List<StarAndMovie> starsInMovies = new ArrayList<>();
    List<Star> stars = new ArrayList<>();

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
                    Movie movie = parseMovie(movieElement);
                    System.out.println(movie);
                    if (movie != null) {
                        movies.add(movie);
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
                System.out.println("Star in Movie: " + filmId + ", " + stageName);
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
                }
                System.out.println("Star: " + stageName + ", Birth Year: " + birthYear);
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

        String title = getTextValue(element, "t");
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Skipping movie with no title, ID: " + id);
            return null;
        }

        int year = getIntValue(element, "year");
        if (year == -1) {
            System.out.println("Skipping movie with invalid year: " + title);
            return null;
        }

        String director = getTextValue(element, "dirn");
        if (director == null || director.trim().isEmpty() || director.startsWith("UnYear")) {
            System.out.println("Skipping movie with invalid director, ID: " + id);
            return null;
        }

        List<Genre> movieGenres = parseGenres(element);
        Movie movie = new Movie(id, title, year, director);
        movie.setGenres(movieGenres);

        for (Genre genre : movieGenres) {
            GenreAndMovie genreMovie = new GenreAndMovie(genre.getGenre(), id);
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

        String sqlInsertMovie = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
        String sqlInsertStar = "INSERT INTO stars (name, birthYear) VALUES (?, ?)";
        String sqlInsertStarMovie = "INSERT INTO stars_in_movies (starName, movieId) VALUES (?, ?)";
        String sqlInsertGenre = "INSERT INTO genres (name) VALUES (?)";
        String sqlInsertGenreMovie = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";

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

            // Insert movies
            for (Movie movie : movies) {
                psInsertMovie.setString(1, movie.getId());
                psInsertMovie.setString(2, movie.getTitle());
                psInsertMovie.setInt(3, movie.getYear());
                psInsertMovie.setString(4, movie.getDirector());
                psInsertMovie.addBatch();
            }

            // Insert stars
            for (Star star : stars) {
                psInsertStar.setString(1, star.getName());
                if (star.getDob() != null) {
                    psInsertStar.setInt(2, star.getDob());
                } else {
                    psInsertStar.setNull(2, Types.INTEGER);
                }
                psInsertStar.addBatch();
            }

            // Insert genres
            for (Genre genre : genres) {
                psInsertGenre.setString(1, genre.getGenre());
                psInsertGenre.addBatch();
            }

            // Execute batches for movies, stars, and genres
            psInsertMovie.executeBatch();
            psInsertStar.executeBatch();
            psInsertGenre.executeBatch();

            // Get generated keys for genres and create a map
            Map<String, Integer> genreNameToIdMap = new HashMap<>();
            ResultSet generatedKeys = psInsertGenre.getGeneratedKeys();
            while (generatedKeys.next()) {
                int genreId = generatedKeys.getInt(1);
                String genreName = generatedKeys.getString(2);
                genreNameToIdMap.put(genreName, genreId);
            }

            // Insert stars_in_movies
            for (StarAndMovie starMovie : starsInMovies) {
                psInsertStarMovie.setString(1, starMovie.getStageName());
                psInsertStarMovie.setString(2, starMovie.getMovieId());
                psInsertStarMovie.addBatch();
            }

            // Insert genres_in_movies
            for (GenreAndMovie genreMovie : genresInMovies) {
                Integer genreId = genreNameToIdMap.get(genreMovie.getGenreId());
                if (genreId != null) {
                    psInsertGenreMovie.setInt(1, genreId);
                    psInsertGenreMovie.setString(2, genreMovie.getMovieId());
                    psInsertGenreMovie.addBatch();
                }
            }

            // Execute batches for relationships
            psInsertStarMovie.executeBatch();
            psInsertGenreMovie.executeBatch();

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


        public static void main(String[] args) {
        DomParser actualParser = new DomParser();

        actualParser.run();
    }
}

