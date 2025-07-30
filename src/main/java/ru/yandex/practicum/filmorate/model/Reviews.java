package ru.yandex.practicum.filmorate.model;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reviews {

    private Long id;
    private Long userId;
    private Long filmId;
    private String content;
    private Boolean isPositive;
    private int useful;

}
