package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewLikes {
    private long  reviewId;
    private long userId;
    private Boolean isLike;
}
