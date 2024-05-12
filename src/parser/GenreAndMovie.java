package parser;

public class GenreAndMovie {
    private final String genreId;
    private final String movieId;

    public GenreAndMovie(String genreId, String movieId) {
        this.genreId = genreId;
        this.movieId = movieId;
    }

    public String getGenreId() {
        return genreId;
    }

    public String getMovieId() {
        return movieId;
    }

    @Override
    public String toString() {
        return "GenreAndMovie{" +
                "genreId='" + getGenreId() + '\'' +
                ", movieId='" + getMovieId() + '\'' +
                '}';
    }
}
