package ru.yandex.practicum.filmorate.storage.filmStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> filmMap = new HashMap<>();
    private static int counter = 0;

    @Override
    public Film create(Film film) {
        film.setId(generateId());
        filmMap.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Film existingFilm = filmMap.get(film.getId());
        if (existingFilm == null) {
            throw new EntityNotFoundException("Фильм с ID " + film.getId() + "не найден");
        }
        filmMap.put(film.getId(), film);
        log.debug("Фильм обновлён: {}", film);
        return film;
    }

    @Override
    public void deleteById(Integer id) {
        Film removed = filmMap.remove(id);
        if (removed == null) {
            throw new EntityNotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public Optional<Film> getById(Integer id) {
        return Optional.ofNullable(filmMap.get(id));
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmMap.values();
    }

    private int generateId() {
        return ++counter;
    }
}
