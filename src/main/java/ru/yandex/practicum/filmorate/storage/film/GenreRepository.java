package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String FIND_ALL = "SELECT * FROM genres";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_BY_FILM = "SELECT g.* FROM film_genres fg " +
            "JOIN genres g ON fg.genre_id = g.genre_id " +
            "WHERE fg.film_id = ?";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Optional<Genre> findById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL);
    }

    public List<Genre> findByFilmId(long filmId) {
        return findMany(FIND_BY_FILM, filmId);
    }
}
