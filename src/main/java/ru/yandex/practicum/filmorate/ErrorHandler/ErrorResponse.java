package ru.yandex.practicum.filmorate.ErrorHandler;

public record ErrorResponse(
        String message,
        int status,
        String path
) {
}
