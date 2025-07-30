package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorRepository;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public Director create(Director director) {
        directorRepository.create(director);
        log.info("Добавлен новый режиссер \"{}\" c id {}", director.getName(), director.getId());
        return director;
    }

    public Director update(Director director) {
        directorRepository.findById(director.getId())
                .orElseThrow(() -> {
                    log.error("Режиссер с id {} не найден", director.getId());
                    return new NotFoundException("Режиссер с id " + director.getId() + " не найден");
                });

        directorRepository.update(director);
        log.info("Режиссер c id {} обновлен", director.getId());
        return director;
    }

    public Collection<Director> getDirectors() {
        return directorRepository.getDirectors();
    }

    public Director getDirectorById(long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + id + " не найден"));
    }

    public void delete(long id) {
        if (!directorRepository.delete(id)) {
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }
        log.info("Режиссер с id = {} удален", id);
    }
}
