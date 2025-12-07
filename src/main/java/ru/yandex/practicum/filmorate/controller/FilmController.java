package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> filmMap = new HashMap<>();
    private static int counter = 0;

    @PostMapping
    public Film create(@RequestBody Film newFilm) {
        log.info("POST /films — попытка создать фильм: {}", newFilm.getName());
        validateFilm(newFilm);
        newFilm.setId(generateId());
        filmMap.put(newFilm.getId(), newFilm);
        log.debug("Фильм создан: {}", newFilm);
        return newFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("PUT /films — попытка обновить фильм с ID {}", newFilm.getId());
        Film existingFilm = filmMap.get(newFilm.getId());
        if (existingFilm == null) {
            log.warn("Попытка обновить несуществующий фильм с ID {}", newFilm.getId());
            throw new ValidateException("Фильм с ID " + newFilm.getId() + "не найден");
        }
        validateFilm(newFilm);
        filmMap.put(newFilm.getId(), newFilm);
        log.debug("Фильм обновлён: {}", newFilm);
        return newFilm;
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("GET /films — попытка вернуть список фильмов, всего: {}", filmMap.size());
        return filmMap.values();
    }

    private void validateFilm(Film film) {
        List<String> errors = Stream.of(
                        (film.getName() == null || film.getName().isBlank())
                                ? "название не может быть пустым" : null,
                        (film.getDescription().length() >= 200)
                                ? "максимальная длина описания — 200 символов" : null,
                        (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)))
                                ? "дата релиза — не раньше 28 декабря 1895 года" : null,
                        (film.getDuration().isZero() || film.getDuration().isNegative()) ? "продолжительность фильма должна быть положительным числом" : null)
                .filter(Objects::nonNull)
                .toList();
        if (!errors.isEmpty()) {
            String errorMsg = String.join("; ", errors);
            log.warn("Ошибка валидации фильма {}: {}", film.getName(), errorMsg);
            throw new ValidateException(errorMsg);
        }
    }

    private int generateId() {
        return ++counter;
    }
}
