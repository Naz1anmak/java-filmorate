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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@Deprecated
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
class FilmoRateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreRepository genreStorage;
    private final MpaRepository mpaStorage;
    private final LikeRepository likeStorage;

    private Long userId1, userId2;
    private Long filmId1, filmId2;

    @BeforeEach
    void initData() {
        User u1 = User.builder()
                .email("a@a.com").login("a").name("A").birthday(LocalDate.of(1980, 1, 1))
                .build();
        User u2 = User.builder()
                .email("b@b.com").login("b").name("B").birthday(LocalDate.of(1990, 2, 2))
                .build();
        userId1 = userStorage.create(u1).getId();
        userId2 = userStorage.create(u2).getId();

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
    }

    @Test
    void testAddUser() {
        User x = User.builder()
                .email("x@x.com").login("x").name("X").birthday(LocalDate.of(2001, 1, 1))
                .build();
        User saved = userStorage.create(x);

        Optional<User> found = userStorage.findById(saved.getId());
        assertThat(found).isPresent()
                .get()
                .hasFieldOrPropertyWithValue("login", "x")
                .hasFieldOrPropertyWithValue("email", "x@x.com")
                .hasFieldOrPropertyWithValue("name", "X");
    }

    @Test
    void testGetUserById() {
        Optional<User> opt = userStorage.findById(userId1);
        assertThat(opt).isPresent()
                .get().hasFieldOrPropertyWithValue("id", userId1);
    }

    @Test
    void testGetAllUsers() {
        Collection<User> all = userStorage.getUsers();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateUser() {
        User u = userStorage.findById(userId1).orElseThrow();
        u.setName("NewName");
        u.setEmail("new@a.com");
        userStorage.update(u);

        assertThat(userStorage.findById(userId1))
                .get().hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("email", "new@a.com");
    }

    @Test
    void testFriendsLifecycle() {
        assertThat(userStorage.getFriends(userId1)).isEmpty();

        userStorage.addFriend(userId1, userId2);
        assertThat(userStorage.getFriends(userId1))
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(userId2);

        userStorage.deleteFriend(userId1, userId2);
        assertThat(userStorage.getFriends(userId1)).isEmpty();
    }

    @Test
    void testAddFilm() {
        Film f = Film.builder()
                .name("Solaris")
                .description("Sci-fi")
                .releaseDate(LocalDate.of(1972, 5, 5))
                .duration(169)
                .mpaRating(new MpaRating(2, "PG"))
                .genres(new LinkedHashSet<>(Set.of(new Genre(2, "Драма"), new Genre(4, "Триллер"))))
                .build();
        Film saved = filmStorage.create(f);

        Optional<Film> opt = filmStorage.findById(saved.getId());
        assertThat(opt).isPresent()
                .get()
                .hasFieldOrPropertyWithValue("name", "Solaris")
                .hasFieldOrPropertyWithValue("description", "Sci-fi")
                .hasFieldOrPropertyWithValue("mpaRating", new MpaRating(2, "PG"));
    }

    @Test
    void testGetFilmById() {
        Optional<Film> opt = filmStorage.findById(filmId1);
        assertThat(opt).isPresent()
                .get().hasFieldOrPropertyWithValue("id", filmId1);
    }

    @Test
    void testGetAllFilms() {
        Collection<Film> films = filmStorage.getFilms();
        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateFilm() {
        Film f = filmStorage.findById(filmId2).orElseThrow();
        f.setName("UpdatedName");
        filmStorage.update(f);

        assertThat(filmStorage.findById(filmId2))
                .get().hasFieldOrPropertyWithValue("name", "UpdatedName");
    }

    @Test
    void testTopFilmsAndLikes() {
        likeStorage.addLike(filmId1, userId1);

        var top1 = filmStorage.findTopFilms(1, null, null);
        assertThat(top1)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(filmId1);
    }

    @Test
    void testGenresAndMpaLoaded() {
        var allGenres = genreStorage.findAll();
        assertThat(allGenres).hasSizeGreaterThanOrEqualTo(2);

        var allMpa = mpaStorage.findAll();
        assertThat(allMpa).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void getAllMpa_returnsNonEmptyList() {
        List<MpaRating> list = mpaStorage.findAll();
        assertThat(list).isNotEmpty()
                .allSatisfy(m -> assertThat(m.getId()).isPositive());
    }

    @Test
    void addAndFindLikesByFilm() {
        likeStorage.addLike(filmId1, userId1);
        likeStorage.addLike(filmId1, userId2);

        Set<Long> likes = likeStorage.findLikesByFilm(filmId1);
        assertThat(likes).containsExactlyInAnyOrder(userId1, userId2);
    }

    @Test
    void deleteLike_removesEntry() {
        likeStorage.addLike(filmId2, userId1);
        assertThat(likeStorage.findLikesByFilm(filmId2)).contains(userId1);

        likeStorage.deleteLike(filmId2, userId1);
        assertThat(likeStorage.findLikesByFilm(filmId2)).isEmpty();
    }

    @Test
    void findByFilmIds_groupsCorrectly() {
        likeStorage.addLike(filmId1, userId1);
        likeStorage.addLike(filmId1, userId2);
        likeStorage.addLike(filmId2, userId2);

        Map<Long, Set<Long>> map = likeStorage.findByFilmIds(List.of(filmId1, filmId2));
        assertThat(map.keySet()).containsExactlyInAnyOrder(filmId1, filmId2);
        assertThat(map.get(filmId1)).containsExactlyInAnyOrder(userId1, userId2);
        assertThat(map.get(filmId2)).containsExactly(userId2);
    }
}
