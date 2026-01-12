package ru.yandex.practicum.filmorate.errorHandler;

public record ErrorResponse(
        String message,
        int status,
        String path
) {
}
