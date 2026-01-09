package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.model.Film.Genre;
import ru.yandex.practicum.filmorate.repository.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.repository.LikeRepository;
import ru.yandex.practicum.filmorate.repository.repository.UserRepository;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Сервис для работы с фильмами.
 * Содержит бизнес-логику, работает напрямую с репозиториями.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final GenreService genreService;

    /**
     * Создать новый фильм.
     */
    @Transactional
    public Film create(Film film) {
        log.info("Попытка создать фильм: {}", film.getName());

        // Валидация жанров перед сохранением
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreService.validateGenres(film.getGenres());
        }

        // Создание фильма
        Film created = filmRepository.create(film);

        // Загрузка жанров из БД (с правильными данными)
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> genres = genreRepository.getGenresByFilmId(created.getId());
            created.setGenres(genres);
        }

        log.debug("Фильм создан с ID={}", created.getId());
        return created;
    }

    /**
     * Обновить фильм.
     */
    @Transactional
    public Film update(Film film) {
        log.info("Попытка обновить фильм с ID {}", film.getId());

        // Проверка существования
        filmRepository.findById(film.getId())
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + film.getId() + " не найден"));

        // Валидация жанров
        if (film.getGenres() != null) {
            genreService.validateGenres(film.getGenres());
        }

        // Обновление фильма
        Film updated = filmRepository.update(film);

        // Загрузка жанров из БД
        Set<Genre> genres = genreRepository.getGenresByFilmId(updated.getId());
        updated.setGenres(genres);

        log.debug("Фильм с ID {} обновлен", film.getId());
        return updated;
    }

    /**
     * Получить все фильмы.
     */
    public Collection<Film> getAllFilms() {
        log.info("Получение всех фильмов");
        return filmRepository.findAll();
    }

    /**
     * Получить фильм по ID.
     */
    public Film getFilmById(Integer id) {
        log.info("Получение фильма с ID {}", id);
        return filmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + id + " не найден"));
    }

    /**
     * Удалить фильм.
     */
    @Transactional
    public void deleteFilm(Integer id) {
        log.info("Удаление фильма с ID {}", id);

        // Проверка существования
        filmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + id + " не найден"));

        boolean deleted = filmRepository.deleteById(id);

        if (!deleted) {
            throw new EntityNotFoundException("Не удалось удалить фильм с id " + id);
        }

        log.info("Фильм с ID {} удален", id);
    }

    /**
     * Добавить лайк фильму от пользователя.
     */
    @Transactional
    public void addLike(Integer filmId, Integer userId) {
        log.info("Пользователь с ID {} пытается поставить лайк фильму с ID {}", userId, filmId);

        // Проверка существования фильма
        filmRepository.findById(filmId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + filmId + " не найден"));

        // Проверка существования пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        // Проверка что лайк еще не поставлен
        if (likeRepository.hasLike(filmId, userId)) {
            throw new IllegalStateException("Лайк уже поставлен пользователем с ID " + userId);
        }

        // Добавление лайка
        likeRepository.addLike(filmId, userId);

        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);
    }

    /**
     * Удалить лайк фильма от пользователя.
     */
    @Transactional
    public void removeLike(Integer filmId, Integer userId) {
        log.info("Пользователь с ID {} пытается убрать лайк с фильма с ID {}", userId, filmId);

        // Проверка существования фильма
        filmRepository.findById(filmId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + filmId + " не найден"));

        // Проверка существования пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        // Удаление лайка
        likeRepository.removeLike(filmId, userId);

        log.info("Пользователь с ID {} убрал лайк с фильма с ID {}", userId, filmId);
    }

    /**
     * Получить топ самых популярных фильмов.
     * @param limit Количество фильмов
     * @return Список фильмов, отсортированных по количеству лайков
     */
    public Collection<Film> getTopMostLikedFilms(int limit) {
        log.info("Получение топ-{} самых популярных фильмов", limit);

        return filmRepository.findAll().stream()
                .sorted(Comparator.comparingInt((Film film) ->
                                likeRepository.countLikes(film.getId()))
                        .reversed())
                .limit(limit)
                .toList();
    }
}