package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class LikeRepository {
    private final JdbcTemplate jdbc;

    private static final String ADD_LIKE_SQL =
            "INSERT INTO user_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_SQL =
            "DELETE FROM user_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_BY_FILM_SQL =
            "SELECT user_id FROM user_likes WHERE film_id = ?";

    public void addLike(long filmId, long userId) {
        jdbc.update(ADD_LIKE_SQL, filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        jdbc.update(DELETE_LIKE_SQL, filmId, userId);
    }

    public Set<Long> findLikesByFilm(long filmId) {
        return new HashSet<>(jdbc.queryForList(FIND_LIKES_BY_FILM_SQL, Long.class, filmId));
    }
}
