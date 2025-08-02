package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    boolean delete(Long filmId);

    Collection<Film> getFilms();

    Optional<Film> findById(Long filmId);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> findTopFilms(int count, Long genreId, Integer year);

    List<Film> findRecommendedFilms(Long userId);

    List<Film> findByDirectorSorted(Long directorId, String sortBy);

    List<Film> findByTitle(String query);

    List<Film> findByDirector(String query);

    List<Film> findByTitleAndDirector(String query);
}