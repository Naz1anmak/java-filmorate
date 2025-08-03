package ru.yandex.practicum.filmorate.service.reviews;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.review.Reviews;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.reviews.ReviewsStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewsService {
    private final ReviewsStorage reviewsStorage;
    private final ReviewLikeService reviewLikeService;
    private final UserService userService;
    private final FilmService filmService;
    private final EventService eventService;

    public Reviews create(Reviews reviews) {
        validateAndChange(reviews);
        reviewsStorage.create(reviews);
        eventService.saveEvent(reviews.getUserId(), reviews.getId(), EventType.REVIEW, EventOperation.ADD);
        return reviews;
    }

    public Reviews update(Reviews reviews) {
        validateAndChange(reviews);
        reviewsStorage.update(reviews);
        Reviews reviewsTrue = reviewsStorage.findById(reviews.getId()).get();
        eventService.saveEvent(reviewsTrue.getUserId(), reviewsTrue.getId(), EventType.REVIEW, EventOperation.UPDATE);
        return reviewsTrue;
    }

    public void delete(long id) {
        findById(id);
        eventService.saveEvent(findById(id).getUserId(), id, EventType.REVIEW, EventOperation.REMOVE);
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
        findById(id);
        userService.findById(userId);
        //eventService.saveEvent(userId, id, EventType.LIKE, EventOperation.ADD);
        reviewLikeService.addLike(id, userId);
    }

    public void addDislike(long id, long userId) {
        findById(id);
        userService.findById(userId);
        //eventService.saveEvent(userId, id, EventType.LIKE, EventOperation.ADD);
        reviewLikeService.addDislike(id, userId);
    }

    public void deleteLike(long id, long userId) {
        findById(id);
        userService.findById(userId);
        //eventService.saveEvent(userId, id, EventType.LIKE, EventOperation.REMOVE);
        reviewLikeService.deleteLike(id, userId);
    }

    public void deleteDislike(long id, long userId) {
        findById(id);
        userService.findById(userId);
        //eventService.saveEvent(userId, id, EventType.LIKE, EventOperation.REMOVE);
        reviewLikeService.deleteLike(id, userId);
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

        userService.findById(reviews.getUserId());
        filmService.findById(reviews.getFilmId());
    }
}
