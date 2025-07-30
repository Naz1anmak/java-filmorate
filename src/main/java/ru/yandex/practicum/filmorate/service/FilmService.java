package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreRepository;
import ru.yandex.practicum.filmorate.storage.film.LikeRepository;
import ru.yandex.practicum.filmorate.storage.film.MpaRepository;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final UserService userService;
    private final FilmStorage filmStorage;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;
    private final UserStorage userStorage;

    public Film create(Film film) {
        validateAndChange(film);
        filmStorage.create(film);
        log.info("Добавлен новый фильм \"{}\" c id {}", film.getName(), film.getId());
        return film;
    }

    public Film update(Film film) {
        filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + film.getId() + " не найден"));

        validateAndChange(film);
        filmStorage.update(film);
        log.info("Фильм c id {} обновлен", film.getId());
        return film;
    }

    public void delete(Long filmId) {
        if (!filmStorage.delete(filmId)) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        log.info("Фильм с id = {} удален", filmId);
    }

    public Film findById(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userService.findById(userId);

        Set<Long> likes = likeRepository.findLikesByFilm(filmId);
        if (likes.contains(userId)) {
            throw new IllegalArgumentException("Пользователь уже ставил лайк этому фильму");
        }

        likeRepository.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userService.findById(userId);

        likeRepository.deleteLike(filmId, userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.findTopFilms(count);
    }

    public List<Film> getRecommendations(Long userId) {
        userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с идентификатором " + userId + " не найден")
        );
        return filmStorage.findRecommendedFilms(userId);
    }

    private Film validateAndChange(Film film) {
        MpaRating mpa = mpaRepository.findById(film.getMpaRating().getId())
                .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpaRating().getId() + " не найден"));
        film.setMpaRating(mpa);

        Set<Genre> genres = film.getGenres() == null
                ? Set.of()
                : film.getGenres();

        genres.forEach(g ->
                genreRepository.findById(g.getId())
                        .orElseThrow(() ->
                                new NotFoundException("Жанр с id=" + g.getId() + " не найден")
                        )
        );
        film.setGenres(genres);

        return film;
    }
}