package ru.yandex.practicum.filmorate.model;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Long userId;
    private Long filmId;
    private String content;
    private Boolean isPositive;
    private int useful;

}
