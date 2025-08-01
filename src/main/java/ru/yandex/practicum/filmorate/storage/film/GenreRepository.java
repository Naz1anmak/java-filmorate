package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Optional<Genre> findById(long genreId) {
        return findOne(FIND_BY_ID, genreId);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL);
    }

    public List<Genre> findByFilmId(long filmId) {
        return findMany(FIND_BY_FILM, filmId);
    }

    public Map<Long, Set<Genre>> findByFilmIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) return Map.of();
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        List<GenreRow> rows = jdbc.query(sql,
                (rs, rn) -> new GenreRow(
                        rs.getLong("film_id"),
                        new Genre(rs.getInt("genre_id"), rs.getString("name"))
                ),
                filmIds.toArray()
        );

        return rows.stream()
                .collect(Collectors.groupingBy(
                        GenreRow::getFilmId,
                        Collectors.mapping(GenreRow::getGenre, Collectors.toSet())
                ));
    }

    public boolean existsById(long id) {
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private static class GenreRow {
        private final Long filmId;
        private final Genre genre;

        GenreRow(Long filmId, Genre genre) {
            this.filmId = filmId;
            this.genre = genre;
        }

        Long getFilmId() {
            return filmId;
        }

        Genre getGenre() {
            return genre;
        }
    }
}
