package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Collection<Film> getFilms();

    Optional<Film> findById(Long filmId);

    List<Film> findTopFilms(int count);
}
