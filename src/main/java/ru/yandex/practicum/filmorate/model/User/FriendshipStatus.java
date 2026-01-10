package ru.yandex.practicum.filmorate.model.User;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public enum FriendshipStatus {
    PENDING,
    CONFIRMED;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;
}
