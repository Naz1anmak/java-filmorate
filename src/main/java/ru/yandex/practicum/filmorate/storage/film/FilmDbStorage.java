package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
            "WHERE film_id = ?";
    public static final String INSERT_FILM_GENRES_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    public static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String TOP_N_SQL =
            "SELECT f.*, COUNT(ul.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN user_likes ul ON f.film_id = ul.film_id " +
                    "GROUP BY f.film_id " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film create(Film film) {
        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating().getId()
        );
        film.setId(id);

        jdbc.update(DELETE_FILM_GENRES_QUERY, id);
        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                try {
                    jdbc.update(INSERT_FILM_GENRES_QUERY, id, g.getId());
                } catch (DuplicateKeyException ignored) {
                }
            }
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        int updated = jdbc.update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating().getId(),
                film.getId()
        );
        if (updated == 0) {
            throw new NotFoundException("Фильм не найден");
        }

        jdbc.update(DELETE_FILM_GENRES_QUERY, film.getId());
        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                jdbc.update(INSERT_FILM_GENRES_QUERY,
                        film.getId(), g.getId());
            }
        }
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    @Override
    public List<Film> findTopFilms(int count) {
        return jdbc.query(TOP_N_SQL, mapper, count);
    }
}
