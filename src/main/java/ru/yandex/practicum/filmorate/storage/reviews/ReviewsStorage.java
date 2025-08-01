package ru.yandex.practicum.filmorate.storage.reviews;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.review.Reviews;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewsStorage extends BaseRepository<Reviews> {

    public ReviewsStorage(JdbcTemplate jdbc, RowMapper<Reviews> mapper) {
        super(jdbc, mapper);
    }

    private static final String INSERT_REVIEW = "INSERT INTO reviews (user_id, film_id, content, is_positive, useful) VALUES (?, ?, ?, ?, 0)";

    private static final String UPDATE_REVIEW = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";

    private static final String DELETE_REVIEW = "DELETE FROM reviews WHERE review_id = ?";

    private static final String GET_ID_REVIEW = "SELECT * FROM reviews WHERE review_id = ?";

    private static final String GET_REVIEW_FILM_ID = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

    private static final String GET_REVIEW = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";

    public void create(Reviews review) {
        long id = insert(INSERT_REVIEW,
                review.getUserId(),
                review.getFilmId(),
                review.getContent(),
                review.getIsPositive());
        review.setId(id);
    }

    public void update(Reviews review) {
        int updated = jdbc.update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getId());
    }

    public boolean delete(long id) {
        int deleted = jdbc.update(DELETE_REVIEW, id);
        return deleted > 0;
    }

    public Optional<Reviews> findById(Long id) {
        return findOne(GET_ID_REVIEW, id);
    }

    public List<Reviews> findAll(int count) {
        return jdbc.query(GET_REVIEW, mapper, count);
    }

    public List<Reviews> findByFilmId(Long filmId, int count) {
        return jdbc.query(GET_REVIEW_FILM_ID, mapper, filmId, count);
    }
}
