package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.ReviewLikes;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewLikesRowMapper implements RowMapper<ReviewLikes> {
    @Override
    public ReviewLikes mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ReviewLikes.builder()
                .reviewId(rs.getLong("review_id"))
                .userId(rs.getLong("user_id"))
                .isLike(rs.getBoolean("is_like"))
                .build();
    }
}
