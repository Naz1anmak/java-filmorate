package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.*;

@Slf4j
@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private final FilmRowMapper filmRowMapper;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;
    private final LikeRepository likeRepository;
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, " +
            "release_date, duration, mpa_rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, " +
            "release_date = ?, duration = ?, mpa_rating_id = ? " +
            "WHERE film_id = ?";
    public static final String INSERT_FILM_GENRES_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    public static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String TOP_N_SQL =
            "SELECT f.* " +
                    "FROM films f " +
                    "LEFT JOIN user_likes ul ON f.film_id = ul.film_id " +
                    "GROUP BY f.film_id " +
                    "ORDER BY COUNT(ul.user_id) DESC " +
                    "LIMIT ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, FilmRowMapper filmRowMapper,
                         GenreRepository genreRepository, MpaRepository mpaRepository, LikeRepository likeRepository) {
        super(jdbc, mapper);
        this.filmRowMapper = filmRowMapper;
        this.genreRepository = genreRepository;
        this.mpaRepository = mpaRepository;
        this.likeRepository = likeRepository;
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
            for (Genre genre : film.getGenres()) {
                try {
                    jdbc.update(INSERT_FILM_GENRES_QUERY, id, genre.getId());
                } catch (DuplicateKeyException ignored) {
                    log.warn("Попытка добавить дублирующийся жанр {} для фильма {}", genre.getName(), film.getName());
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
            for (Genre genre : film.getGenres()) {
                try {
                    jdbc.update(INSERT_FILM_GENRES_QUERY, film.getId(), genre.getId());
                } catch (DuplicateKeyException ignored) {
                    log.warn("Попытка добавить дублирующийся жанр {} для фильма {}", genre.getName(), film.getName());
                }
            }
        }
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        List<Film> films = jdbc.query(FIND_ALL_QUERY, filmRowMapper);
        enrichFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, filmId);
        film.ifPresent(f -> enrichFilms(List.of(f)));
        return film;
    }

    @Override
    public List<Film> findTopFilms(int count) {
        List<Film> films = jdbc.query(TOP_N_SQL, filmRowMapper, count);
        enrichFilms(films);
        return films;
    }

    private void enrichFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        Map<Long, Set<Genre>> genresByFilm = genreRepository.findByFilmIds(filmIds);
        Map<Long, MpaRating> mpaByFilm = mpaRepository.findByFilmIds(filmIds);
        Map<Long, Set<Long>> likesByFilm = likeRepository.findByFilmIds(filmIds);

        films.forEach(f -> {
            f.setGenres(genresByFilm.getOrDefault(f.getId(), Set.of()));
            f.setMpaRating(mpaByFilm.get(f.getId()));
            f.getMovieRating().clear();
            f.getMovieRating().addAll(likesByFilm.getOrDefault(f.getId(), Set.of()));
        });
    }

    @Override
    public boolean existsById(Long filmId) {
        return findById(filmId).isPresent();
    }
}