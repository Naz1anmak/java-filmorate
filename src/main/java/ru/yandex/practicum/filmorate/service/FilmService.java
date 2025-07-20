package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {
    private final UserService userService;
    private final FilmStorage filmStorage;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;


    @Autowired
    public FilmService(UserService userService, @Qualifier("filmDbStorage") FilmStorage filmStorage,
                       MpaRepository mpaRepository, GenreRepository genreRepository, LikeRepository likeRepository) {
        this.userService = userService;
        this.filmStorage = filmStorage;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.likeRepository = likeRepository;
    }

    public Film create(Film film) {
        film = film.toBuilder().build();

        Film finalFilm = film;
        MpaRating mpa = mpaRepository.findById(film.getMpaRating().getId())
                .orElseThrow(() -> new NotFoundException("MPA с id=" + finalFilm.getMpaRating().getId() + " не найден"));

        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                genreRepository.findById(g.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + g.getId() + " не найден"));
            }
        }

        film.setMpaRating(mpa);
        film.setGenres(film.getGenres() == null ? Set.of() : film.getGenres());

        filmStorage.create(film);
        log.info("Добавлен новый фильм \"{}\" c id {}", film.getName(), film.getId());
        return film;
    }

    public Film update(Film film) {
        Film oldFilm = filmStorage.getFilms().stream()
                .filter(f -> f.getId().equals(film.getId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", film.getId());
                    return new NotFoundException("Фильм с id " + film.getId() + " не найден");
                });

        MpaRating mpa = mpaRepository.findById(film.getMpaRating().getId())
                .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpaRating().getId() + " не найден"));

        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                genreRepository.findById(g.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + g.getId() + " не найден"));
            }
        }

        film.setMpaRating(mpa);
        film.setGenres(film.getGenres() == null ? Set.of() : film.getGenres());

        filmStorage.update(film);
        log.info("Фильм c id {} обновлен", film.getId());
        return film;
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
}