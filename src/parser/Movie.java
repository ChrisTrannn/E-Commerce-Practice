package parser;

import java.util.List;

public class Movie {

    private final String id;

    private final String title;

    private final int year;

    private final String director;

    private List<Genre> genres;

    public Movie(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public String getId() {
        return id;
    }

    public String getTitle() { return title; }

    public int getYear() { return year; }

    public String getDirector() {
        return director;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }


    public String toString() {

        return "ID:" + getId() + ", " +
                "Title:" + getTitle() + ", " +
                "Year:" + getYear() + ", " +
                "Director:" + getDirector() + "," +
                "Genres:" + getGenres();
    }
}
