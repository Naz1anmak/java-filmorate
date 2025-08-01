package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.film.LikeRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    public void addLike(long filmId, long userId) {
        likeRepository.addLike(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        likeRepository.deleteLike(filmId, userId);
    }

    public Set<Long> findLikesByFilm(long filmId) {
        return likeRepository.findLikesByFilm(filmId);
    }

    public Map<Long, Set<Long>> findByFilmIds(List<Long> filmIds) {
        return likeRepository.findByFilmIds(filmIds);
    }
}
