package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director {
    private Long id;

    @NotBlank(message = "Имя режиссера не должно быть пустым")
    private String name;
}
