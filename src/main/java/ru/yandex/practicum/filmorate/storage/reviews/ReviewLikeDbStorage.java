package ru.yandex.practicum.filmorate.storage.reviews;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.ReviewLikes;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

@Repository
public class ReviewLikeDbStorage extends BaseRepository<ReviewLikes> {

    public ReviewLikeDbStorage(JdbcTemplate jdbc, RowMapper<ReviewLikes> mapper) {
        super(jdbc, mapper);
    }

    private static final String ADD_LIKE = "INSERT INTO review_likes (reviewId, userId, isPositive) VALUES (?, ?, TRUE)";

    private static final String ADD_DISLIKE = "INSERT INTO review_likes (reviewId, userId, isPositive) VALUES (?, ?, FALSE)";

    private static final String DELETE_LIKE = "DELETE FROM review_likes WHERE reviewId = ? AND userId = ?";

    private static final String UPDATE_LIKE = """
            UPDATE reviews SET useful = (
                SELECT COALESCE(SUM(CASE WHEN is_positive THEN 1 ELSE -1 END), 0)
                FROM review_likes
                WHERE reviewId = ?
            )
            WHERE id = ?
        """;

    public void addLike(long reviewId, long userId) {
        jdbc.update(ADD_LIKE, reviewId, userId);
        updateUseful(reviewId);
    }

    public void addDislike(long reviewId, long userId) {
        jdbc.update(ADD_DISLIKE, reviewId, userId);
        updateUseful(reviewId);
    }

    public void deleteLike(long reviewId, long userId) {
        jdbc.update(DELETE_LIKE, reviewId, userId);
        updateUseful(reviewId);
    }

    private void updateUseful(long reviewId) {
        jdbc.update(UPDATE_LIKE, reviewId, reviewId);
    }
}
