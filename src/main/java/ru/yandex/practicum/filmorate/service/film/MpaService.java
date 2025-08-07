package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.MpaRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaRepository mpaRepository;

    public MpaRating getMpaById(long mpaId) {
        return mpaRepository.findById(mpaId)
                .orElseThrow(() -> new NotFoundException("MPA с id=" + mpaId + " не найден"));
    }

    public List<MpaRating> getAllMpa() {
        return mpaRepository.findAll();
    }

    public Map<Long, MpaRating> findByFilmIds(List<Long> filmIds) {
        return mpaRepository.findByFilmIds(filmIds);
    }
}