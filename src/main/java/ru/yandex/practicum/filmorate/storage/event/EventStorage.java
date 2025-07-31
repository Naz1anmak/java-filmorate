package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Event> eventRowMapper;

    @Transactional
    public void saveEvent(Long userId, Long entityId, EventType eventType, EventOperation eventOperation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "INSERT INTO events (user_id, entity_id, timestamp, event_type, event_operation) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
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

    @Transactional
    public List<Event> getFeed(Long userId) {
        String query = """
                SELECT *
                FROM events
                WHERE user_id = ?
                ORDER BY timestamp
                """;
        return jdbc.query(query, eventRowMapper, userId);
    }

}
