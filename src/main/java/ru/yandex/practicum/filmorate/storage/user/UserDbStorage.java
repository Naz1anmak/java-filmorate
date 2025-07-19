package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ?" +
            " WHERE user_id = ?";

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
    public Collection<User> getUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return findOne(FIND_BY_ID_QUERY, userId);
    }
}
