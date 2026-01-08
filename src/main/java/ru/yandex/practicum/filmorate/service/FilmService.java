package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage.UserStorage;

import java.util.Collection;
import java.util.Comparator;


@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;
    private final UserStorage userStorage;

    public void addLike(Integer filmId, Integer userId) {
        log.info("Пользователь с ID {} пытается поставить лайк фильму с ID {}", userId, filmId);
        getByIdOrThrowNotFound(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));
        if (likeStorage.hasLike(filmId, userId)) {
            throw new IllegalStateException("Лайк уже поставлен пользователем с ID " + userId);
        }
        likeStorage.addLike(filmId, userId);
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.info("Пользователь с ID {} пытается убрать лайк с фильма с ID {}", userId, filmId);
        getByIdOrThrowNotFound(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));
        likeStorage.removeLike(filmId, userId);
        log.info("Пользователь с ID {} убрал лайк с фильма с ID {}", userId, filmId);
    }

    public Collection<Film> getTopMostLikedFilms(int limit) {
        return getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> likeStorage.countLikes(film.getId()))
                        .reversed())
                .limit(limit)
                .toList();
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

    public Film getFilmById(Integer id) {
        log.info("Получение фильма с ID {}", id);
        return getByIdOrThrowNotFound(id);
    }

    public void deleteFilm(Integer id) {
        log.info("Удаление фильма с ID {}", id);
        getByIdOrThrowNotFound(id);
        filmStorage.deleteById(id);
        log.info("Фильм с ID {} удален", id);
    }

    private Film getByIdOrThrowNotFound(Integer id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + id + " не найден"));
    }
}