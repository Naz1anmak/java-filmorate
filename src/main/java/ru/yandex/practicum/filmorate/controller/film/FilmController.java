package ru.yandex.practicum.filmorate.controller.film;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long filmId) {
        filmService.delete(filmId);
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable("id") Long filmId) {
        return filmService.findById(filmId);
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getByDirector(
            @PathVariable Long directorId,
            @RequestParam(name = "sortBy", defaultValue = "likes") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> findByTitleOrDirector(
            @RequestParam String query,
            @RequestParam(defaultValue = "title") String by) {
        return filmService.findByTitleOrDirector(query, by);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "friendId") Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }
}