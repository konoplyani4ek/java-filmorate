package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public void addLike(Integer filmId, Integer userId) {
        log.info("Пользователь с ID {} пытается поставить лайк фильму с ID {}", userId, filmId);
        Film film = getByIdOrThrowNotFound(filmId);
        userService.getUserById(userId); // проверка существует ли юзер
        film.addLike(userId);
        log.info("Пользователь с ID" + userId + "поставил лайк фильму с ID " + filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.info("Пользователь с ID {} пытается убрать лайк с фильма с ID {}", userId, filmId);
        Film film = getByIdOrThrowNotFound(filmId);
        userService.getUserById(userId); // проверка существует ли юзер
        film.removeLike(userId);
        log.info("Пользователь с ID" + userId + "убрал лайк с фильма с ID " + filmId);
    }

    public Collection<Film> getTopMostLikedFilms(int limit) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikedByUserId().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Film create(Film newFilm) {
        log.info("попытка создать фильм: {}", newFilm.getName());
        filmStorage.create(newFilm);
        log.debug("Фильм создан, ID= {}", newFilm.getId());
        return newFilm;
    }

    public Film update(Film newFilm) {
        log.info("попытка обновить фильм с ID {}", newFilm.getId());
        return filmStorage.update(newFilm);
    }

    public Collection<Film> getAllFilms() {
        log.info("Вывод всех фильмов");
        return filmStorage.getAllFilms();
    }

    private Film getByIdOrThrowNotFound(Integer id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + id + " не найден"));
    }
}
