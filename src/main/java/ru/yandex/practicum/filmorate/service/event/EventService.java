package ru.yandex.practicum.filmorate.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventStorage;

    public void saveEvent(Long userId, Long entityId, EventType eventType, EventOperation eventOperation) {
        eventStorage.saveEvent(userId, entityId, eventType, eventOperation);
    }

    public List<Event> getFeed(Long userId) {
        return eventStorage.getFeed(userId);
    }
}
