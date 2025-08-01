package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreRepository;
import ru.yandex.practicum.filmorate.storage.film.LikeRepository;
import ru.yandex.practicum.filmorate.storage.film.MpaRepository;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        UserDbStorage.class,
        FilmDbStorage.class,
        GenreRepository.class,
        LikeRepository.class,
        MpaRepository.class,
        UserRowMapper.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        MpaRowMapper.class
})
public class TestDeleteFilmAndUser {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreRepository genreStorage;
    private final MpaRepository mpaStorage;
    private final LikeRepository likeStorage;

    private Long filmId1, filmId2;
    private Long userId1, userId2;

    @BeforeEach
    public void beforeEach() {
        Film f1 = Film.builder()
                .name("Film1")
                .description("Desc1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpaRating(new MpaRating(1, "G"))
                .genres(new LinkedHashSet<>(Set.of(new Genre(1, "Комедия"))))
                .build();
        Film f2 = Film.builder()
                .name("Film2")
                .description("Desc2")
                .releaseDate(LocalDate.of(2010, 2, 2))
                .duration(120)
                .mpaRating(new MpaRating(2, "PG"))
                .genres(new LinkedHashSet<>(Set.of(new Genre(2, "Драма"))))
                .build();
        filmId1 = filmStorage.create(f1).getId();
        filmId2 = filmStorage.create(f2).getId();

        User u1 = User.builder()
                .email("a@a.com").login("a").name("A").birthday(LocalDate.of(1980, 1, 1))
                .build();
        User u2 = User.builder()
                .email("b@b.com").login("b").name("B").birthday(LocalDate.of(1990, 2, 2))
                .build();
        userId1 = userStorage.create(u1).getId();
        userId2 = userStorage.create(u2).getId();
    }

    // Тест успешного удаления фильма по id
    @Test
    public void deleteFilm_shouldReturnTrueWhenFilmExists() {
        // Проверка, что фильм существует перед удалением
        assertThat(filmStorage.findById(filmId1))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("id", filmId1);

        // Удаление фильма
        boolean isDeleted = filmStorage.delete(filmId1);

        // Проверка удаления
        assertThat(isDeleted)
                .withFailMessage("Ожидалось true при успешном удалении")
                .isTrue();

        // Проверка, что фильм больше не доступен
        assertThat(filmStorage.findById(filmId1))
                .withFailMessage("Фильм должен быть удален")
                .isEmpty();

        // Проверка, что другие фильмы не затронуты
        assertThat(filmStorage.findById(filmId2))
                .withFailMessage("Другие фильмы не должны быть удалены")
                .isPresent();
    }

    // Тест удаления несуществующего фильма с id 999L
    @Test
    public void deleteFilm_shouldReturnFalseWhenFilmNotExists() {
        // Проверка, что фильма действительно нет
        assertThat(filmStorage.findById(999L))
                .withFailMessage("Фильма с id:999L не должно существовать")
                .isEmpty();

        // Попытка удалить несуществующий фильм
        boolean isDeleted = filmStorage.delete(999L);

        assertThat(isDeleted)
                .withFailMessage("Ожидалось false при попытке удалить несуществующий фильм")
                .isFalse();
    }

    // Тест успешного удаления пользователя по id
    @Test
    public void deleteUser_shouldReturnTrueWhenUserExists() {
        // Проверка, что пользователь существует перед удалением
        assertThat(userStorage.findById(userId1))
                .isPresent()
                .get()
                .hasFieldOrPropertyWithValue("id", userId1);

        // Удаление пользователя
        boolean isDeleted = userStorage.delete(userId1);

        // Проверка удаления
        assertThat(isDeleted)
                .withFailMessage("Ожидалось true при успешном удалении")
                .isTrue();

        // Проверка, что пользователь больше не доступен
        assertThat(userStorage.findById(userId1))
                .withFailMessage("Пользователь должен быть удален")
                .isEmpty();

        // Проверка, что другие пользователи не затронуты
        assertThat(userStorage.findById(userId2))
                .withFailMessage("Другие пользователи не должны быть удалены")
                .isPresent();
    }

    // Тест удаления несуществующего пользователя с id 999L
    @Test
    public void deleteUser_shouldReturnFalseWhenUserNotExists() {
        // Проверка, что пользователя действительно нет
        assertThat(userStorage.findById(999L))
                .withFailMessage("Пользователя с id:999L не должно существовать")
                .isEmpty();

        // Попытка удалить несуществующего пользователя
        boolean isDeleted = userStorage.delete(999L);

        assertThat(isDeleted)
                .withFailMessage("Ожидалось false при попытке удалить несуществующего пользователя")
                .isFalse();
    }
}