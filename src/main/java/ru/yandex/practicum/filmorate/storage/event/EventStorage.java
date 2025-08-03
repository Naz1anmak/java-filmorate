package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

@Repository
public class EventStorage extends BaseRepository<Event> {
    private static final String SAVE_EVENT = "INSERT INTO events (user_id, entity_id, " +
                                             "timestamp, event_type, event_operation) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_FEED = " SELECT * FROM events WHERE user_id = ? ORDER BY timestamp";

    public EventStorage(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper);
    }

    public void saveEvent(Long userId, Long entityId, EventType eventType, EventOperation eventOperation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SAVE_EVENT, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, entityId);
            ps.setLong(3, Instant.now().toEpochMilli());
            ps.setString(4, eventType.name());
            ps.setString(5, eventOperation.name());
            return ps;
        }, keyHolder);
        Long eventId = keyHolder.getKeyAs(Long.class);
        if (eventId == null) throw new InternalServerException("Не удалось сгенерировать идентификатор события");
    }

    public List<Event> getFeed(Long userId) {
        return findMany(GET_FEED, userId);
    }
}
