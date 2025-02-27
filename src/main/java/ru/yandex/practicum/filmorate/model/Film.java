package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import ru.yandex.practicum.filmorate.validation.ReleaseDateValid;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Film.FilmBuilder.class)
public class Film {
    int id;

    @NotNull(message = "Название не должно быть null")
    @NotBlank(message = "Название не должно быть пустым")
    String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;

    @ReleaseDateValid
    LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    int duration;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FilmBuilder {

    }
}
