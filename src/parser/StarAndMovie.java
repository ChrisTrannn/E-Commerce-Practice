package parser;

public class StarAndMovie {
    private String stageName;
    private String movieId;

    public StarAndMovie(String stageName, String movieId) {
        this.stageName = stageName;
        this.movieId = movieId;
    }

    public String getStageName() {
        return stageName;
    }

    public String getMovieId() {
        return movieId;
    }

    @Override
    public String toString() {
        return "StarsAndMovies{" +
                "stageName='" + stageName + '\'' +
                ", movieId='" + movieId + '\'' +
                '}';
    }
}
