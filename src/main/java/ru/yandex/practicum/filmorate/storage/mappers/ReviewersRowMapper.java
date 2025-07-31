package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.review.Reviews;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewersRowMapper implements RowMapper<Reviews> {
    @Override
    public Reviews mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Reviews.builder()
                .id(rs.getLong("review_id"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .useful(rs.getInt("useful"))
                .build();
    }
}
