package ru.yandex.practicum.filmorate.ErrorHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(
            EntityNotFoundException e,
            HttpServletRequest request
    ) {
        log.warn("Entity not found: {}", e.getMessage());

        return new ErrorResponse(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return new ErrorResponse(e.getMessage(), 400, request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalState(IllegalStateException e, HttpServletRequest request) {
        return new ErrorResponse(e.getMessage(), 409, request.getRequestURI());
    }

    @ExceptionHandler(ValidateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleValidateException(ValidateException e, HttpServletRequest request) {
        log.warn("Validation error: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value(), request.getRequestURI());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NoSuchElementException e, HttpServletRequest request) {
        log.warn("Not found error: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), 404, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return new ErrorResponse(message, 400, request.getRequestURI());
    }
}



