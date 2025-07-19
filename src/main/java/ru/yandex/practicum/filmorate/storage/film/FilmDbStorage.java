package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, releaseDate, duration) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?," +
            " duration = ? WHERE film_id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film create(Film film) {
        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );
        film.setId(id);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );
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
}
