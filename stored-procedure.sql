use moviedb;

-- add_star stored procedure
DELIMITER //

CREATE PROCEDURE add_star(
    IN p_name VARCHAR(255),
    IN p_birth_year INT,
    OUT p_new_id VARCHAR(10)
)
BEGIN
    DECLARE new_id INT;
    DECLARE new_id_str VARCHAR(10);

    -- find max id and increment by 1
    SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1 INTO new_id FROM stars;

    -- prefix the new_id with nm
    SET new_id_str = CONCAT('nm', LPAD(new_id, 7, '0'));

    -- insert the new star with new_id into stars table
    INSERT INTO stars (id, name, birthYear) VALUES (new_id_str, p_name, p_birth_year);

    -- output the newly generated id
    SET p_new_id = new_id_str;
END //

DELIMITER ;

-- add_movie stored procedure
DELIMITER //

CREATE PROCEDURE add_movie(
    IN p_movie_title VARCHAR(100),
    IN p_movie_year INTEGER,
    IN p_movie_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_star_birth_year INT,
    IN p_genre_name VARCHAR(32),
    OUT p_movie_id VARCHAR(10),
    OUT p_star_id VARCHAR(10),
    OUT p_genre_id INTEGER
)
BEGIN
    -- find max movieId and increment by 1
    SELECT CONCAT('tt', LPAD(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 7, '0')) INTO p_movie_id FROM movies;

    -- check if star exists
    SELECT id INTO p_star_id FROM stars WHERE name = p_star_name LIMIT 1;

    -- create star if it does not exist
    IF p_star_id IS NULL THEN
        -- find max starId and increment by 1
        SELECT CONCAT('nm', LPAD(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 7, '0')) INTO p_star_id FROM stars;

        -- insert the new star with new id into stars table
        INSERT INTO stars (id, name, birthYear) VALUES (p_star_id, p_star_name, p_star_birth_year);
    END IF;

    -- check if genre exists
    SELECT id INTO p_genre_id FROM genres WHERE name = p_genre_name LIMIT 1;

    -- create genre if it does not exist
    IF p_genre_id IS NULL THEN
        -- find max genreId and increment by 1
        SELECT MAX(id) + 1 INTO p_genre_id FROM genres;

        -- insert the new genre with new id into stars table
        INSERT INTO genres (id, name) VALUES (p_genre_id, p_genre_name);
    END IF;

    -- Insert the new movie into the movies table
    INSERT INTO movies (id, title, year, director) VALUES (p_movie_id, p_movie_title, p_movie_year, p_movie_director);

    -- Link the movie to the stars and genres
    INSERT INTO stars_in_movies (starId, movieId) VALUES (p_star_id, p_movie_id);
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (p_genre_id, p_movie_id);

END //

DELIMITER ;