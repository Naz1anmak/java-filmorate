package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = User.UserBuilder.class)
public class User {
    int id;

    @NotNull
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный формат email")
    String email;

    @NotNull
    @NotBlank(message = "Логин не должен быть пустым")
    String login;

    String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserBuilder {

    }
}
