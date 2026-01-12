package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.repository.repository.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final GenreService genreService;
    private final MpaRatingRepository mpaRepository;

    @Transactional
    public Film create(Film film) {
        log.info("Попытка создать фильм: {}", film.getName());

        // Валидация MPA
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            if (!mpaRepository.existsById(film.getMpa().getId())) {
                throw new EntityNotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
            }
        }

        // Валидация и заполнение полных данных жанров ПЕРЕД сохранением
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreService.validateGenres(film.getGenres());

            //  Заполнить полные данные жанров из БД
            Set<Genre> fullGenres = film.getGenres().stream()
                    .map(genre -> genreService.getGenreById(genre.getId()))
                    .collect(Collectors.toSet());
            film.setGenres(fullGenres);
        }

        Film created = filmRepository.create(film);

        // Загрузка фильма со всеми связями из БД
        Film result = getFilmById(created.getId());

        log.debug("Фильм создан с ID={}", result.getId());
        return result;
    }

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

        Film updated = filmRepository.update(film);

        // Загрузка жанров из БД
        Set<Genre> genres = genreRepository.getGenresByFilmId(updated.getId());
        updated.setGenres(genres);

        log.debug("Фильм с ID {} обновлен", film.getId());
        return updated;
    }

    public Collection<Film> getAllFilms() {
        log.info("Получение всех фильмов");
        return filmRepository.findAll();
    }

    public Film getFilmById(Integer id) {
        log.info("Получение фильма с ID {}", id);
        return filmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + id + " не найден"));
    }

    @Transactional
    public void deleteFilm(Integer id) {
        log.info("Удаление фильма с ID {}", id);

        filmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + id + " не найден"));

        boolean deleted = filmRepository.deleteById(id);

        if (!deleted) {
            throw new EntityNotFoundException("Не удалось удалить фильм с id " + id);
        }

        log.info("Фильм с ID {} удален", id);
    }

    @Transactional
    public void addLike(Integer filmId, Integer userId) {
        log.info("Пользователь с ID {} пытается поставить лайк фильму с ID {}", userId, filmId);

        filmRepository.findById(filmId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + filmId + " не найден"));

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        if (likeRepository.hasLike(filmId, userId)) {
            log.debug("Лайк уже поставлен пользователем с ID {}, пропускаем", userId);
            return;
        }

        likeRepository.addLike(filmId, userId);
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);
    }

    @Transactional
    public void removeLike(Integer filmId, Integer userId) {
        log.info("Пользователь с ID {} пытается убрать лайк с фильма с ID {}", userId, filmId);

        filmRepository.findById(filmId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм с id " + filmId + " не найден"));

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + userId + " не найден"));

        // Удаление лайка
        likeRepository.removeLike(filmId, userId);

        log.info("Пользователь с ID {} убрал лайк с фильма с ID {}", userId, filmId);
    }

    // Список фильмов, отсортированных по количеству лайков
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