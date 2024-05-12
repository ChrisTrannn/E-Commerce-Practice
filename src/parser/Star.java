package parser;

import java.util.ArrayList;
import java.util.List;

public class Star {
    private final String name;
    private final Integer dob;
    private final List<String> movies;

    public Star(String name, Integer dob) {
        this.name = name;
        this.dob = dob;
        this.movies = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Integer getDob() {
        return dob;
    }

    public List<String> getMovies() {
        return movies;
    }

    public void addMovie(String movieId) {
        if (!movies.contains(movieId)) {
            movies.add(movieId);
        }
    }

    public String toString() {
        return "Star: " + getName() + ", Birth Year: " + getDob() + ", Movies: " + getMovies().toString();
    }
}
