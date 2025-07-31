package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLikes {

    @NotNull
    private Long reviewId;

    @NotNull
    private Long userId;

    @NotNull
    private Boolean isLike;
}
