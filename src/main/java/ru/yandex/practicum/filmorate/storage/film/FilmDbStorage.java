package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.service.film.DirectorService;
import ru.yandex.practicum.filmorate.service.film.GenreService;
import ru.yandex.practicum.filmorate.service.film.LikeService;
import ru.yandex.practicum.filmorate.service.film.MpaService;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.*;

@Slf4j
@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private final FilmRowMapper filmRowMapper;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final DirectorService directorService;
    private final LikeService likeService;
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
    private static final String DELETE_FILM_BY_ID_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String INSERT_FILM_DIRECTORS_QUERY =
            "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_QUERY =
            "DELETE FROM film_directors WHERE film_id = ?";
    private static final String FIND_BY_TITLE_QUERY = "SELECT f.*, COUNT(ul.user_id) AS likes_count " +
            "FROM films f " +
            "LEFT JOIN user_likes ul ON f.film_id = ul.film_id " +
            "WHERE LOWER(f.name) LIKE LOWER('%' || ? || '%') " +
            "GROUP BY f.film_id " +
            "ORDER BY likes_count DESC";
    private static final String FIND_BY_DIRECTORS_QUERY = "SELECT f.*, COUNT(ul.user_id) AS likes_count " +
            "FROM films f " +
            "JOIN film_directors fd ON f.film_id = fd.film_id " +
            "JOIN directors d ON fd.director_id = d.director_id " +
            "LEFT JOIN user_likes ul ON f.film_id = ul.film_id " +
            "WHERE LOWER(d.name) LIKE LOWER('%' || ? || '%') " +
            "GROUP BY f.film_id " +
            "ORDER BY likes_count DESC";
    private static final String FIND_BY_TITLE_OR_DIRECTORS_QUERY = "SELECT f.*, COUNT(ul.user_id) AS likes_count " +
            "FROM films f " +
            "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.director_id " +
            "LEFT JOIN user_likes ul ON f.film_id = ul.film_id " +
            "WHERE LOWER(f.name) LIKE LOWER('%' || ? || '%') " +
            "OR LOWER(d.name) LIKE LOWER('%' || ? || '%') " +
            "GROUP BY f.film_id " +
            "ORDER BY likes_count DESC";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, FilmRowMapper filmRowMapper,
                         GenreService genreService, MpaService mpaService, DirectorService directorService, LikeService likeService) {
        super(jdbc, mapper);
        this.filmRowMapper = filmRowMapper;
        this.genreService = genreService;
        this.mpaService = mpaService;
        this.directorService = directorService;
        this.likeService = likeService;
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
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, id);
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update(INSERT_FILM_DIRECTORS_QUERY, id, director.getId());
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
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update(INSERT_FILM_DIRECTORS_QUERY, film.getId(), director.getId());
            }
        }
        return film;
    }

    @Override
    public boolean delete(Long filmId) {
        return delete(DELETE_FILM_BY_ID_QUERY, filmId);
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

    @Override
    @Transactional
    public List<Film> findRecommendedFilms(Long userId) {
        String query = """
                WITH user_likes_films_CTE AS (
                    SELECT film_id, user_id\s
                    FROM user_likes\s
                    WHERE user_id = ?),
                most_similar_user_by_likes_films_CTE AS (
                    SELECT ul1.user_id, count(*) AS total\s
                    FROM user_likes ul1\s
                    JOIN user_likes_films_CTE ul2 on ul2.film_id = ul1.film_id\s
                    AND NOT ul2.user_id = ul1.user_id
                    GROUP BY ul1.user_id\s
                    ORDER BY total DESC\s
                    LIMIT 1),
                most_similar_user_likes_films_CTE AS (
                    SELECT film_id, ul3.user_id\s
                    FROM user_likes ul3
                    JOIN most_similar_user_by_likes_films_CTE ul4 on ul3.user_id = ul4.user_id),
                recommended_films_CTE AS (
                    SELECT ul5.film_id\s
                    FROM most_similar_user_likes_films_CTE ul5\s
                    LEFT JOIN user_likes_films_CTE ul6 on ul6.film_id = ul5.film_id\s
                    WHERE ul6.user_id IS NULL)
                SELECT *
                FROM films f
                JOIN recommended_films_CTE ul7 on f.film_id = ul7.film_id
                ORDER BY film_id""";
        List<Film> films = jdbc.query(query, filmRowMapper, userId);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> findByDirectorSorted(Long directorId, String sortBy) {
        String orderClause = switch (sortBy) {
            case "year" -> "f.release_date";
            case "likes" -> "(SELECT COUNT(*) FROM user_likes ul WHERE ul.film_id = f.film_id) DESC";
            default -> throw new IllegalArgumentException("sortBy must be 'year' or 'likes'");
        };

        String sql = """
                SELECT f.film_id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_rating_id
                FROM films AS f
                JOIN film_directors fd ON f.film_id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY\s""" + orderClause;

        List<Film> films = jdbc.query(sql, filmRowMapper, directorId);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> findByTitle(String query) {
        List<Film> films = jdbc.query(FIND_BY_TITLE_QUERY, filmRowMapper, query);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> findByDirector(String query) {
        List<Film> films = jdbc.query(FIND_BY_DIRECTORS_QUERY, filmRowMapper, query);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> findByTitleAndDirector(String query) {
        List<Film> films = jdbc.query(FIND_BY_TITLE_OR_DIRECTORS_QUERY, filmRowMapper, query, query);
        enrichFilms(films);
        return films;
    }

    private void enrichFilms(List<Film> films) {
        if (films.isEmpty()) return;

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        Map<Long, Set<Genre>> genresByFilm = genreService.findByFilmIds(filmIds);
        Map<Long, MpaRating> mpaByFilm = mpaService.findByFilmIds(filmIds);
        Map<Long, Set<Director>> directorsByFilm = directorService.findByFilmIds(filmIds);
        Map<Long, Set<Long>> likesByFilm = likeService.findByFilmIds(filmIds);

        films.forEach(f -> {
            f.setGenres(genresByFilm.getOrDefault(f.getId(), Set.of()));
            f.setMpaRating(mpaByFilm.get(f.getId()));
            f.setDirectors(directorsByFilm.getOrDefault(f.getId(), Set.of()));
            f.getMovieRating().clear();
            f.getMovieRating().addAll(likesByFilm.getOrDefault(f.getId(), Set.of()));
        });
    }

    @Override
    public boolean existsById(Long filmId) {
        return findById(filmId).isPresent();
    }
}