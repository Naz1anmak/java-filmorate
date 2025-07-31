package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.review.Reviews;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.reviews.ReviewLikeDbStorage;
import ru.yandex.practicum.filmorate.storage.reviews.ReviewsStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewsService {

    private final ReviewsStorage reviewsStorage;
    private final ReviewLikeDbStorage reviewLikeDbStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Reviews create(Reviews reviews) {
        validateAndChange(reviews);
        reviewsStorage.create(reviews);
        return reviews;
    }

    public Reviews update(Reviews reviews) {
        validateAndChange(reviews);
        reviewsStorage.update(reviews);
        return reviews;
    }

    public void delete(long id) {
        if (!reviewsStorage.existsById(id)) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
        reviewsStorage.delete(id);
    }

    public Reviews findById(long id) {
        return reviewsStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    public List<Reviews> findAll(Long filmId, int count) {
        if (filmId == null) {
            return reviewsStorage.findAll(count);
        } else
            return reviewsStorage.findByFilmId(filmId, count);
    }

    public void addLike(long id, long userId) {
        if (!reviewsStorage.existsById(id)) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        reviewLikeDbStorage.addLike(id, userId);
    }

    public void addDislike(long id, long userId) {
        if (!reviewsStorage.existsById(id)) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        reviewLikeDbStorage.addDislike(id, userId);
    }

    public void deleteLike(long id, long userId) {
        if (!reviewsStorage.existsById(id)) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        reviewLikeDbStorage.deleteLike(id, userId);
    }

    public void deleteDislike(long id, long userId) {
        if (!reviewsStorage.existsById(id)) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        reviewLikeDbStorage.deleteLike(id, userId);
    }

    private void validateAndChange(Reviews reviews) {
        if (reviews.getUserId() == null) {
            throw new ValidationException("Поле userId не может быть null");
        }
        if (reviews.getFilmId() == null) {
            throw new ValidationException("Поле filmId не может быть null");
        }
        if (reviews.getContent() == null || reviews.getContent().trim().isEmpty()) {
            throw new ValidationException("Отзыв не может быть пустым");
        }

        if (!userStorage.existsById(reviews.getUserId())) {
            throw new NotFoundException("Пользователь с id " + reviews.getUserId() + " не найден");
        }
        if (!filmStorage.existsById(reviews.getFilmId())) {
            throw new NotFoundException("Фильм с id " + reviews.getFilmId() + " не найден");
        }
    }
}
