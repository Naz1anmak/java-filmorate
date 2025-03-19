package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        Film createdFilm = film.toBuilder().id(getNextId()).build();
        films.put(createdFilm.getId(), createdFilm);
        log.info("Добавлен новый фильм \"{}\" c id {}", createdFilm.getName(), createdFilm.getId());
        return createdFilm;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Фильм c id {} обновлен", film.getId());
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    private Long getNextId() {
        Long nextId = films.isEmpty() ? 1 : Collections.max(films.keySet()) + 1;
        log.trace("Сгенерирован id: {}", nextId);
        return nextId;
    }
}