package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film.Genre;

import java.util.*;

@Service
@Slf4j
public class GenreService {

    public Collection<Genre> getAllGenres() {
        log.info("Получение всех жанров");
        List<Genre> genres = Arrays.asList(Genre.values());
        log.debug("Найдено жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(Integer id) {
        log.info("Получение жанра с id={}", id);
        if (id < 1 || id > Genre.values().length) {
            log.warn("Жанр с id={} не найден", id);
            throw new EntityNotFoundException("Жанр с id " + id + " не найден");
        }
        Genre genre = Genre.values()[id - 1];
        log.debug("Найден жанр: {}", genre.getRussianName());
        return genre;
    }
}