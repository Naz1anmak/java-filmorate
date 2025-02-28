package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        Film createdFilm = film.toBuilder().id(getNextId()).build();
        films.put(createdFilm.getId(), createdFilm);
        log.info("Добавлен новый фильм \"{}\" c id {}", createdFilm.getName(), createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Фильм c id {} обновлен", film.getId());
        return film;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        log.debug("Films: {}", films);
        return films.values();
    }

    private int getNextId() {
        log.trace("Сгенерирован id: {}", films.isEmpty() ? 1 : Collections.max(films.keySet()) + 1);
        return films.isEmpty() ? 1 : Collections.max(films.keySet()) + 1;
    }
}
