package ru.yandex.practicum.filmorate.model.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reviews {

    @JsonProperty("reviewId")
    private Long id;

    @NotNull(message = "userId не должен быть null")
    private Long userId;

    @NotNull(message = "filmId не должен быть null")
    private Long filmId;

    @NotBlank(message = "content не должен быть пустым")
    private String content;

    @NotNull(message = "isPositive не должен быть null")
    private Boolean isPositive;

    private long useful = 0;
}
