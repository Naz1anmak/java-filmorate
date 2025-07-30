package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.*;

@Deprecated
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean delete(Long userId) {
        return users.remove(userId) != null;
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {

    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {

    }

    @Override
    public List<User> getFriends(Long userId) {
        return List.of();
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return List.of();
    }

    @Override
    public boolean hasFriendRequest(Long userId, Long friendId) {
        return false;
    }

    @Override
    public void confirmFriendship(Long userId, Long friendId) {

    }
}