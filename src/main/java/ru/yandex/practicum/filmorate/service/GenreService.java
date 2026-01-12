package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.repository.repository.GenreRepository;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public Collection<Genre> getAllGenres() {
        log.info("Получение всех жанров");
        Collection<Genre> genres = genreRepository.findAll();
        log.debug("Найдено жанров: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(Integer id) {
        log.info("Получение жанра с id={}", id);
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Жанр с id={} не найден", id);
                    return new EntityNotFoundException("Жанр с id " + id + " не найден");
                });
        log.debug("Найден жанр: {}", genre.getName());
        return genre;
    }

    public boolean existsById(Integer id) {
        return genreRepository.existsById(id);
    }

    public void validateGenres(Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        for (Genre genre : genres) {
            if (genre.getId() == null) {
                throw new IllegalArgumentException("ID жанра не может быть null");
            }
            if (!existsById(genre.getId())) {
                throw new EntityNotFoundException("Жанр с id " + genre.getId() + " не найден");
            }
        }
    }
}