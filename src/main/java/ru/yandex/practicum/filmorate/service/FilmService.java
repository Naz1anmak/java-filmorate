package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.*;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final UserService userService;
    private final FilmStorage filmStorage;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;
    private final UserStorage userStorage;
    private final DirectorRepository directorRepository;
    private final EventStorage eventStorage;

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
        eventStorage.saveEvent(userId, filmId, EventType.LIKE, EventOperation.ADD);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userService.findById(userId);

        likeRepository.deleteLike(filmId, userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", user.getName(), film.getName());
        eventStorage.saveEvent(userId, filmId, EventType.LIKE, EventOperation.REMOVE);
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

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorRepository.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер с id=" + directorId + " не найден"));
        return filmStorage.findByDirectorSorted(directorId, sortBy);
    }

    private void validateAndChange(Film film) {
        MpaRating mpa = mpaRepository.findById(film.getMpaRating().getId())
                .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpaRating().getId() + " не найден"));
        film.setMpaRating(mpa);

        Set<Genre> genres = Optional.ofNullable(film.getGenres()).orElse(Set.of());
        genres.forEach(g -> genreRepository.findById(g.getId())
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + g.getId() + " не найден")));
        film.setGenres(genres);

        Set<Director> directors = Optional.ofNullable(film.getDirectors()).orElse(Set.of());
        directors.forEach(d -> directorRepository.findById(d.getId())
                .orElseThrow(() -> new NotFoundException("Режиссер с id=" + d.getId() + " не найден")));
        film.setDirectors(directors);
    }
}