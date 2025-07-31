package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        userStorage.create(user);
        log.info("Добавлен новый юзер \"{}\" c id {}", user.getLogin(), user.getId());
        return user;
    }

    public User update(User user) {
        userStorage.findById(user.getId())
                .orElseThrow(() -> {
                    log.error("Юзер с id {} не найден", user.getId());
                    return new NotFoundException("Юзер с id " + user.getId() + " не найден");
                });

        userStorage.update(user);
        log.info("Юзер c id {} обновлен", user.getId());
        return user;
    }

    public void delete(Long userId) {
        if (!userStorage.delete(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        log.info("Пользователь с id = {} удален", userId);
    }

    public User findById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        User user = findById(userId);
        User friend = findById(friendId);

        userStorage.addFriend(userId, friendId);
        log.info("{} отправил заявку {} на добавление в друзья!", user.getName(), friend.getName());

        if (userStorage.hasFriendRequest(friendId, userId)) {
            userStorage.confirmFriendship(userId, friendId);
            userStorage.confirmFriendship(friendId, userId);
            log.info("{} и {} подтвердили дружбу!", user.getName(), friend.getName());
        }
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }
        User user = findById(userId);
        User friend = findById(friendId);

        userStorage.deleteFriend(userId, friendId);
        log.info("{} и {} больше не друзья!", user.getName(), friend.getName());
    }

    public List<User> commonFriends(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        return userStorage.getCommonFriends(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        return userStorage.getFriends(userId);
    }
}