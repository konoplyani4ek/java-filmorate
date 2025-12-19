package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

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
            throw new NoSuchElementException("Фильм с ID " + film.getId() + "не найден");
        }
        filmMap.put(film.getId(), film);
        log.debug("Фильм обновлён: {}", film);
        return film;
    }

    @Override // дописать логи?
    public void deleteById(Integer id) {
        Film removed = filmMap.remove(id);
        if (removed == null) {
            throw new NoSuchElementException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public Optional<Film> getById(Integer id) {
        Film film = filmMap.get(id);
        return Optional.ofNullable(film);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmMap.values();
    }

    private int generateId() {
        return ++counter;
    }
}
