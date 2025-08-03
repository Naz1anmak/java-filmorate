package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void delete(long directorId) {
        if (!directorRepository.delete(directorId)) {
            throw new NotFoundException("Режиссер с id = " + directorId + " не найден");
        }
        log.info("Режиссер с id = {} удален", directorId);
    }

    public Collection<Director> getDirectors() {
        return directorRepository.getDirectors();
    }

    public Director getDirectorById(long directorId) {
        return directorRepository.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + directorId + " не найден"));
    }

    public Map<Long, Set<Director>> findByFilmIds(List<Long> filmIds) {
        return directorRepository.findByFilmIds(filmIds);
    }
}
