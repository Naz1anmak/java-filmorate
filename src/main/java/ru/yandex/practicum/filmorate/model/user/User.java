package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    Long id;

    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный формат email")
    String email;

    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^$|^\\S+$", message = "Логин не должен содержать пробелы")
    String login;

    String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;

//    final Set<Long> friends = new HashSet<>();
//    FriendshipStatus friendshipStatus = FriendshipStatus.PENDING;

    @Builder(toBuilder = true)
    public User(Long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;

        if (name == null || name.isBlank()) {
            this.name = login;
        }
    }
}