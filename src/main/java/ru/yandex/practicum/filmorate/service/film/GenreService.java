package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public Genre getGenreById(long genreId) {
        return genreRepository.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + genreId + " не найден"));
    }

    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    public Map<Long, Set<Genre>> findByFilmIds(List<Long> filmIds) {
        return genreRepository.findByFilmIds(filmIds);
    }
}
