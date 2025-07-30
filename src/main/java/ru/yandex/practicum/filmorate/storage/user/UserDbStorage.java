package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                                               "WHERE user_id = ?";
    private static final String INSERT_FRIENDSHIP_QUERY = "INSERT INTO friendship (user_id, friend_id, status) " +
                                                          "VALUES (?, ?, 'PENDING')";
    public static final String DELETE_FRIENDSHIP_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    public static final String FIND_FRIENDS_QUERY = "SELECT u.* FROM users u " +
                                                    "JOIN friendship f ON u.user_id = f.friend_id " +
                                                    "WHERE f.user_id = ?";
    public static final String FIND_COMMON_FRIENDS_QUERY = "SELECT u.* FROM users u " +
                                                           "JOIN friendship f1 ON u.user_id = f1.friend_id " +
                                                           "JOIN friendship f2 ON u.user_id = f2.friend_id " +
                                                           "WHERE f1.user_id = ? AND f2.user_id = ?";
    private static final String DELETE_USER_BY_ID_QUERY = "DELETE FROM users WHERE user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        long id = insert(INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public boolean delete(Long userId) {
        return delete(DELETE_USER_BY_ID_QUERY, userId);
    }

    @Override
    public Collection<User> getUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return findOne(FIND_BY_ID_QUERY, userId);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(INSERT_FRIENDSHIP_QUERY, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbc.update(DELETE_FRIENDSHIP_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return findMany(FIND_FRIENDS_QUERY, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId, otherId);
    }

    @Override
    public boolean hasFriendRequest(Long userId, Long friendId) {
        return getFriends(friendId).contains(userId);
    }

    @Override
    public void confirmFriendship(Long userId, Long friendId) {
        jdbc.update("UPDATE friendship SET status = 'CONFIRMED' " +
                    "WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }
}