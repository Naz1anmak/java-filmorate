package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LikeRepository {
    private final JdbcTemplate jdbc;

    private static final String ADD_LIKE =
            "INSERT INTO user_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE =
            "DELETE FROM user_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_BY_FILM =
            "SELECT user_id FROM user_likes WHERE film_id = ?";

    public void addLike(long filmId, long userId) {
        jdbc.update(ADD_LIKE, filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        jdbc.update(DELETE_LIKE, filmId, userId);
    }

    public Set<Long> findLikesByFilm(long filmId) {
        return new HashSet<>(jdbc.queryForList(FIND_LIKES_BY_FILM, Long.class, filmId));
    }

    public Map<Long, Set<Long>> findByFilmIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) return Map.of();
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT film_id, user_id FROM user_likes WHERE film_id IN (" + inSql + ")";
        List<LikeRow> rows = jdbc.query(sql,
                (rs, rn) -> new LikeRow(
                        rs.getLong("film_id"),
                        rs.getLong("user_id")),
                filmIds.toArray()
        );
        return rows.stream()
                .collect(Collectors.groupingBy(
                        LikeRow::getFilmId,
                        Collectors.mapping(LikeRow::getUserId, Collectors.toSet())
                ));
    }

    private static class LikeRow {
        private final Long filmId, userId;

        LikeRow(Long filmId, Long userId) {
            this.filmId = filmId;
            this.userId = userId;
        }

        Long getFilmId() {
            return filmId;
        }

        Long getUserId() {
            return userId;
        }
    }
}
