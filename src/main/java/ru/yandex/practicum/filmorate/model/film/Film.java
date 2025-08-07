package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.ReleaseDateValid;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor
public class Film {
    Long id;

    @NotBlank(message = "Название не должно быть пустым")
    String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;

    @ReleaseDateValid
    LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    int duration;

    @JsonDeserialize(as = LinkedHashSet.class)
    Set<Genre> genres;

    @JsonProperty("mpa")
    MpaRating mpaRating;

    Set<Director> directors;

    final Set<Long> movieRating = new HashSet<>();

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setTypes(Set<Genre> genres) {
        this.genres = genres;
    }
}