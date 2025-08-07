package ru.yandex.practicum.filmorate.model.event;

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
public class Event {

    @NotNull(message = "eventId не должен быть null")
    private Long eventId;

    @NotNull(message = "userId не должен быть null")
    private Long userId;

    @NotNull(message = "entityId не должен быть null")
    private Long entityId;

    @NotNull(message = "timestamp не должен быть null")
    private Long timestamp;

    @NotBlank
    private EventType eventType;

    @NotBlank
    private EventOperation operation;
}