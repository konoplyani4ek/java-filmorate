package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> filmMap = new HashMap<>();
    private static int counter = 0;

    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("POST /films — попытка создать фильм: {}", newFilm.getName());
        newFilm.setId(generateId());
        filmMap.put(newFilm.getId(), newFilm);
        log.debug("Фильм создан: {}", newFilm);
        return newFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("PUT /films — попытка обновить фильм с ID {}", newFilm.getId());
        Film existingFilm = filmMap.get(newFilm.getId());
        if (existingFilm == null) {
            log.warn("Попытка обновить несуществующий фильм с ID {}", newFilm.getId());
            throw new ValidateException("Фильм с ID " + newFilm.getId() + "не найден");
        }
        filmMap.put(newFilm.getId(), newFilm);
        log.debug("Фильм обновлён: {}", newFilm);
        return newFilm;
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("GET /films — попытка вернуть список фильмов, всего: {}", filmMap.size());
        return filmMap.values();
    }

    private int generateId() {
        return ++counter;
    }
}
