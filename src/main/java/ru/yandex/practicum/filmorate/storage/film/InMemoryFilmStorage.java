package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.*;

@Deprecated
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public boolean delete(Long filmId) {
        return films.remove(filmId) != null;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public List<Film> findTopFilms(int count) {
        return List.of();
    }

    @Override
    public List<Film> findRecommendationsFilms(Long userId) {
        return null;
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }
}