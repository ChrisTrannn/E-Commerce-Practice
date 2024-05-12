package parser;
import java.util.Objects;

public class Genre {
    private final String name;

    public Genre(String name) {
        this.name = name;
    }

    public String getGenre() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return name.equals(genre.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String toString() {
        return "Genre Name: " + getGenre();
    }
}
