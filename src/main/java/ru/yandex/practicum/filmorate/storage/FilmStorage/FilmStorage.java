package ru.yandex.practicum.filmorate.storage.FilmStorage;

import ru.yandex.practicum.filmorate.model.Film.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    void deleteById(Integer id);

    Optional<Film> getById(Integer id);

    Collection<Film> getAllFilms();

}
