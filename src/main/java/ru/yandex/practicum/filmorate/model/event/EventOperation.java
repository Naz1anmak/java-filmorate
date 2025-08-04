package ru.yandex.practicum.filmorate.model.event;

import lombok.Getter;

@Getter
public enum EventOperation {
    REMOVE,
    ADD,
    UPDATE
}