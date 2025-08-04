package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final LikeService likeService;
    private final DirectorService directorService;
    private final EventService eventService;

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

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        User user = userService.findById(userId);

        eventService.saveEvent(userId, filmId, EventType.LIKE, EventOperation.ADD);
        Set<Long> likes = likeService.findLikesByFilm(filmId);
        if (!likes.contains(userId)) {
            likeService.addLike(filmId, userId);
            log.info("Пользователь {} поставил лайк фильму \"{}\"", user.getName(), film.getName());
        }
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        User user = userService.findById(userId);

        eventService.saveEvent(userId, filmId, EventType.LIKE, EventOperation.REMOVE);
        likeService.deleteLike(filmId, userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public List<Film> getTopFilms(int count, Long genreId, Integer year) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть больше 0");
        }
        if (genreId != null) genreService.getGenreById(genreId);
        if (year != null && year < 0) {
            throw new ValidationException("Год не может быть отрицательным");
        }
        return filmStorage.findTopFilms(count, genreId, year);
    }

    public List<Film> getRecommendations(Long userId) {
        userService.findById(userId);
        return filmStorage.findRecommendedFilms(userId);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorService.getDirectorById(directorId);
        return filmStorage.findByDirectorSorted(directorId, sortBy);
    }

    public List<Film> findByTitleOrDirector(String query, String by) {
        validateSearchParams(query, by);

        String[] searchTypes = by.split(",");
        boolean searchTitle = false;
        boolean searchDirector = false;

        for (String type : searchTypes) {
            String trimmedType = type.trim().toLowerCase();
            if (trimmedType.equals("title")) {
                searchTitle = true;
            } else if (trimmedType.equals("director")) {
                searchDirector = true;
            } else {
                throw new ValidationException("Параметр 'by' может содержать только 'title' или 'director'");
            }
        }
        if (searchTitle && searchDirector) {
            return filmStorage.findByTitleAndDirector(query);
        } else if (searchTitle) {
            return filmStorage.findByTitle(query);
        } else if (searchDirector) {
            return filmStorage.findByDirector(query);
        }
        throw new ValidationException("Параметр 'by' должен содержать 'title' или 'director'");
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userService.findById(userId);
        userService.findById(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    private void validateAndChange(Film film) {
        MpaRating mpa = mpaService.getMpaById(film.getMpaRating().getId());
        film.setMpaRating(mpa);

        Set<Genre> genres = Optional.ofNullable(film.getGenres()).orElse(Set.of());
        genres.forEach(g -> genreService.getGenreById(g.getId()));
       /* if (!genres.isEmpty()) {
            Set<Genre> genresNew = genres.stream().sorted(Comparator.comparingLong(Genre::getId)).collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(genresNew);
        } else {
            film.setGenres(genres);
        }*/
        film.setGenres(genres);

        Set<Director> directors = Optional.ofNullable(film.getDirectors()).orElse(Set.of());
        directors.forEach(d -> directorService.getDirectorById(d.getId()));
        film.setDirectors(directors);
    }

    private void validateSearchParams(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Текст поиска не может быть пустым");
        }
        if (by == null || by.isBlank()) {
            throw new ValidationException("Параметр 'by' не может быть пустым");
        }
    }
}