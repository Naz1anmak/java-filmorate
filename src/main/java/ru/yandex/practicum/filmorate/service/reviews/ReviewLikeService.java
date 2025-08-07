package ru.yandex.practicum.filmorate.service.reviews;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.reviews.ReviewLikeDbStorage;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {
    private final ReviewLikeDbStorage reviewLikeDbStorage;

    public void addLike(long reviewId, long userId) {
        reviewLikeDbStorage.addLike(reviewId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        reviewLikeDbStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(long reviewId, long userId) {
        reviewLikeDbStorage.deleteLike(reviewId, userId);
    }
}
