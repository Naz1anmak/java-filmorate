package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.MpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaRepository mpaRepository;

    public List<MpaRating> getAllMpa() {
        return mpaRepository.findAll();
    }

    public MpaRating getMpaById(int id) {
        return mpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA с id=" + id + " не найден"));
    }
}