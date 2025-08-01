package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MpaRepository extends BaseRepository<MpaRating> {
    private static final String FIND_ALL = "SELECT * FROM mpa_rating";
    private static final String FIND_BY_ID = "SELECT * FROM mpa_rating WHERE mpa_rating_id = ?";

    public MpaRepository(JdbcTemplate jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper);
    }

    public Optional<MpaRating> findById(long mpaId) {
        return findOne(FIND_BY_ID, mpaId);
    }

    public List<MpaRating> findAll() {
        return findMany(FIND_ALL);
    }

    public Map<Long, MpaRating> findByFilmIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) return Map.of();
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT f.film_id, mr.mpa_rating_id, mr.name " +
                "FROM mpa_rating mr " +
                "JOIN films f ON mr.mpa_rating_id = f.mpa_rating_id " +
                "WHERE f.film_id IN (" + inSql + ")";

        List<MpaRepository.MpaRow> rows = jdbc.query(sql,
                (rs, rn) -> new MpaRow(
                        rs.getLong("film_id"),
                        new MpaRating(rs.getInt("mpa_rating_id"), rs.getString("name"))
                ),
                filmIds.toArray()
        );

        return rows.stream()
                .collect(Collectors.toMap(
                        MpaRow::getFilmId,
                        MpaRow::getMpaRating
                ));
    }

    private static class MpaRow {
        private final Long filmId;
        private final MpaRating mpaRating;

        MpaRow(Long filmId, MpaRating mpaRating) {
            this.filmId = filmId;
            this.mpaRating = mpaRating;
        }

        Long getFilmId() {
            return filmId;
        }

        MpaRating getMpaRating() {
            return mpaRating;
        }
    }
}
