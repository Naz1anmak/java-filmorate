package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmoRateApplicationTests.TestConfig.class})
class FilmoRateApplicationTests {
    private final UserDbStorage userStorage;

    @Configuration
    static class TestConfig {
        @Bean
        public RowMapper<User> userRowMapper() {
            return (rs, rowNum) -> User.builder()
                    .id(rs.getLong("user_id"))
                    .email(rs.getString("email"))
                    .login(rs.getString("login"))
                    .name(rs.getString("name"))
                    .birthday(rs.getDate("birthday").toLocalDate())
                    .build();
        }
    }

    @Test
    public void testFindUserById() {
        User newUser = User.builder().build();
        newUser.setEmail("test@example.com");
        newUser.setLogin("testLogin");
        newUser.setName("Test User");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        userStorage.create(newUser);

        Optional<User> userOptional = userStorage.findById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }
}
