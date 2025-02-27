package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        User validUser = checkUser(user);

        validUser = validUser.toBuilder().id(getNextId()).build();
        users.put(validUser.getId(), validUser);
        log.info("Добавлен новый юзер \"{}\" c id {}", validUser.getLogin(), validUser.getId());
        return validUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        User validUser = checkUser(user);
        if (user.getId() >= 0 || users.isEmpty()) {
            if (!users.containsKey(user.getId())) {
                log.error("Юзер с id {} не найден", user.getId());
                throw new NotFoundException("Юзер с id " + user.getId() + " не найден");
            }
        }

        users.put(validUser.getId(), validUser);
        log.info("Юзер c id {} обновлен", validUser.getId());
        return validUser;
    }

    @GetMapping
    public Collection<User> getUsers() {
        log.debug("Users: {}", users);
        return users.values();
    }

    private User checkUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user = user.toBuilder().name(user.getLogin()).build();
            log.debug("Имя пользователя заменено на логин");
        }
        log.debug("Валидация пройдена. Возращенный юзер: {}", user);
        return user;
    }

    private int getNextId() {
        log.trace("Сгенерирован id: {}", users.isEmpty() ? 1 : Collections.max(users.keySet()) + 1);
        return users.isEmpty() ? 1 : Collections.max(users.keySet()) + 1;
    }
}
