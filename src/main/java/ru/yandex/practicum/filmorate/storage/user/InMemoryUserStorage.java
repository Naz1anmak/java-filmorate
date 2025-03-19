package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        user = user.toBuilder().id(getNextId()).build();
        users.put(user.getId(), user);
        log.info("Добавлен новый юзер \"{}\" c id {}", user.getLogin(), user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null
                || users.isEmpty()
                || !users.containsKey(user.getId())) {
            log.error("Юзер с id {} не найден", user.getId());
            throw new NotFoundException("Юзер с id " + user.getId() + " не найден");
        }


        users.put(user.getId(), user);
        log.info("Юзер c id {} обновлен", user.getId());
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    private Long getNextId() {
        Long nextId = users.isEmpty() ? 1 : Collections.max(users.keySet()) + 1;
        log.trace("Сгенерирован id: {}", nextId);
        return nextId;
    }
}