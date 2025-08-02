package ru.yandex.practicum.filmorate.storage.film;

import lombok.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DirectorRepository extends BaseRepository<Director> {
    private static final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM directors WHERE director_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    public Director create(Director director) {
        long id = insert(INSERT_QUERY,
                director.getName()
        );
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        update(
                UPDATE_QUERY,
                director.getName(),
                director.getId()
        );
        return director;
    }

    public boolean delete(long directorId) {
        return delete(DELETE_DIRECTOR_QUERY, directorId);
    }

    public Collection<Director> getDirectors() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Director> findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Map<Long, Set<Director>> findByFilmIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) return Map.of();
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT fd.film_id, d.director_id, d.name " +
                "FROM film_directors fd " +
                "JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE fd.film_id IN (" + inSql + ")";

        List<FilmDirectorRow> rows = jdbc.query(
                sql,
                (rs, rn) -> new FilmDirectorRow(
                        rs.getLong("film_id"),
                        new Director(
                                rs.getLong("director_id"),
                                rs.getString("name")
                        )
                ),
                filmIds.toArray()
        );

        return rows.stream()
                .collect(Collectors.groupingBy(
                        FilmDirectorRow::getFilmId,
                        Collectors.mapping(FilmDirectorRow::getDirector, Collectors.toSet())
                ));
    }

    @Value
    private static class FilmDirectorRow {
        Long filmId;
        Director director;
    }
}
