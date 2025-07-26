package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRepository extends BaseRepository<MpaRating> {
    private static final String FIND_ALL = "SELECT * FROM mpa_rating";
    private static final String FIND_BY_ID = "SELECT * FROM mpa_rating WHERE mpa_rating_id = ?";

    public MpaRepository(JdbcTemplate jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper);
    }

    public Optional<MpaRating> findById(int id) {
        return findOne(FIND_BY_ID, id);
    }

    public List<MpaRating> findAll() {
        return findMany(FIND_ALL);
    }
}
