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
    private static final String FIND_ALL = "SELECT * FROM films";
    private static final String FIND_BY_ID = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, " +
            "release_date, duration, mpa_rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, " +
            "release_date = ?, duration = ?, mpa_rating_id = ? " +
            "WHERE film_id = ?";
    public static final String INSERT_FILM_GENRES = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    public static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String DELETE_FILM_BY_ID = "DELETE FROM films WHERE film_id = ?";
    private static final String INSERT_FILM_DIRECTORS = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String FIND_BY_TITLE = """
            SELECT f.*, COUNT(ul.user_id) AS likes_count
            FROM films f
            LEFT JOIN user_likes ul ON f.film_id = ul.film_id
            WHERE LOWER(f.name) LIKE LOWER('%' || ? || '%')
            GROUP BY f.film_id
            ORDER BY likes_count DESC""";
    private static final String FIND_BY_DIRECTORS = """
            SELECT f.*, COUNT(ul.user_id) AS likes_count
            FROM films f
            JOIN film_directors fd ON f.film_id = fd.film_id
            JOIN directors d ON fd.director_id = d.director_id
            LEFT JOIN user_likes ul ON f.film_id = ul.film_id
            WHERE LOWER(d.name) LIKE LOWER('%' || ? || '%')
            GROUP BY f.film_id
            ORDER BY likes_count DESC""";
    private static final String FIND_BY_TITLE_OR_DIRECTOR = """
            SELECT f.*, COUNT(ul.user_id) AS likes_count
            FROM films f
            LEFT JOIN film_directors fd ON f.film_id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.director_id
            LEFT JOIN user_likes ul ON f.film_id = ul.film_id
            WHERE LOWER(f.name) LIKE LOWER('%' || ? || '%')
            OR LOWER(d.name) LIKE LOWER('%' || ? || '%')
            GROUP BY f.film_id
            ORDER BY likes_count DESC""";

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

        jdbc.update(DELETE_FILM_GENRES, id);
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                try {
                    jdbc.update(INSERT_FILM_GENRES, id, genre.getId());
                } catch (DuplicateKeyException ignored) {
                    log.warn("Попытка добавить дублирующийся жанр {} для фильма {}", genre.getName(), film.getName());
                }
            }
        }
        jdbc.update(DELETE_FILM_DIRECTORS, id);
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update(INSERT_FILM_DIRECTORS, id, director.getId());
            }
        }
        return film;
    }

    @Override
    @Transactional
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

        jdbc.update(DELETE_FILM_GENRES, film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                try {
                    jdbc.update(INSERT_FILM_GENRES, film.getId(), genre.getId());
                } catch (DuplicateKeyException ignored) {
                    log.warn("Попытка добавить дублирующийся жанр {} для фильма {}", genre.getName(), film.getName());
                }
            }
        }
        jdbc.update(DELETE_FILM_DIRECTORS, film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                jdbc.update(INSERT_FILM_DIRECTORS, film.getId(), director.getId());
            }
        }
        return film;
    }

    @Override
    public boolean delete(Long filmId) {
        return delete(DELETE_FILM_BY_ID, filmId);
    }

    @Override
    public Collection<Film> getFilms() {
        List<Film> films = jdbc.query(FIND_ALL, filmRowMapper);
        enrichFilms(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        Optional<Film> film = findOne(FIND_BY_ID, filmId);
        film.ifPresent(f -> enrichFilms(List.of(f)));
        return film;
    }

    @Override
    @Transactional
    public List<Film> findRecommendedFilms(Long userId) {
        String query = """
                WITH user_likes_films_CTE AS (
                    SELECT film_id, user_id
                    FROM user_likes
                    WHERE user_id = ?),
                    most_similar_user_by_likes_films_CTE AS (
                        SELECT ul1.user_id, count(*) AS total
                        FROM user_likes ul1
                        JOIN user_likes_films_CTE ul2 on ul2.film_id = ul1.film_id
                        AND NOT ul2.user_id = ul1.user_id
                        GROUP BY ul1.user_id
                        ORDER BY total DESC
                        LIMIT 1),
                    most_similar_user_likes_films_CTE AS (
                        SELECT film_id, ul3.user_id
                        FROM user_likes ul3
                        JOIN most_similar_user_by_likes_films_CTE ul4 on ul3.user_id = ul4.user_id),
                        recommended_films_CTE AS (
                            SELECT ul5.film_id
                            FROM most_similar_user_likes_films_CTE ul5
                            LEFT JOIN user_likes_films_CTE ul6 on ul6.film_id = ul5.film_id
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
            case "year" -> " f.release_date";
            case "likes" -> " (SELECT COUNT(*) FROM user_likes ul WHERE ul.film_id = f.film_id) DESC";
            default -> throw new IllegalArgumentException(" sortBy must be 'year' or 'likes'");
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
                ORDER BY""" + orderClause;

        List<Film> films = jdbc.query(sql, filmRowMapper, directorId);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> findByTitle(String query) {
        List<Film> films = jdbc.query(FIND_BY_TITLE, filmRowMapper, query);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> findByDirector(String query) {
        List<Film> films = jdbc.query(FIND_BY_DIRECTORS, filmRowMapper, query);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> findByTitleAndDirector(String query) {
        List<Film> films = jdbc.query(FIND_BY_TITLE_OR_DIRECTOR, filmRowMapper, query, query);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = """
                WITH common AS (
                  SELECT film_id
                  FROM user_likes
                  WHERE user_id = ?
                  INTERSECT
                  SELECT film_id
                  FROM user_likes
                  WHERE user_id = ?
                )
                SELECT f.*
                FROM films f
                JOIN common c ON f.film_id = c.film_id
                LEFT JOIN user_likes ul ON f.film_id = ul.film_id
                GROUP BY f.film_id
                ORDER BY COUNT(ul.user_id) DESC
                """;
        List<Film> films = jdbc.query(sql, filmRowMapper, friendId, userId);
        enrichFilms(films);
        return films;
    }

    @Override
    public List<Film> findTopFilms(int count, Long genreId, Integer year) {
        List<Object> params = new ArrayList<>();
        String sql;

        if (genreId != null && year != null) {
            sql = """
                    SELECT f.*
                    FROM films f
                    LEFT JOIN user_likes ul ON f.film_id = ul.film_id
                    JOIN film_genres fg ON f.film_id = fg.film_id
                    WHERE fg.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ?
                    GROUP BY f.film_id
                    ORDER BY COUNT(ul.user_id) DESC
                    LIMIT ?""";
            params.add(genreId);
            params.add(year);
        } else if (genreId != null) {
            sql = """
                    SELECT f.*
                    FROM films f
                    LEFT JOIN user_likes ul ON f.film_id = ul.film_id
                    JOIN film_genres fg ON f.film_id = fg.film_id
                    WHERE fg.genre_id = ?
                    GROUP BY f.film_id
                    ORDER BY COUNT(ul.user_id) DESC
                    LIMIT ?""";
            params.add(genreId);
        } else if (year != null) {
            sql = """
                    SELECT f.*
                    FROM films f
                    LEFT JOIN user_likes ul ON f.film_id = ul.film_id
                    WHERE EXTRACT(YEAR FROM f.release_date) = ?
                    GROUP BY f.film_id
                    ORDER BY COUNT(ul.user_id) DESC
                    LIMIT ?""";
            params.add(year);
        } else {
            sql = """
                    SELECT f.*
                    FROM films f
                    LEFT JOIN user_likes ul ON f.film_id = ul.film_id
                    GROUP BY f.film_id
                    ORDER BY COUNT(ul.user_id) DESC
                    LIMIT ?""";
        }
        params.add(count);
        List<Film> films = jdbc.query(sql, filmRowMapper, params.toArray());
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
}