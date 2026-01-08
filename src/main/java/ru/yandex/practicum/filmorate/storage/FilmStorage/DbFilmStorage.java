package ru.yandex.practicum.filmorate.storage.FilmStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.model.Film.Genre;
import ru.yandex.practicum.filmorate.repository.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.repository.GenreRepository;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Component
@Primary
@RequiredArgsConstructor
public class DbFilmStorage implements FilmStorage {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;

    @Override
    public Film create(Film film) {
        Film saved = filmRepository.create(film);

        // Сохраняем ВСЕ жанры
        genreRepository.setGenresForFilm(saved.getId(), film.getGenres());

        // Возвращаем фильм уже с жанрами
        Set<Genre> genres = genreRepository.getGenresByFilmId(saved.getId());
        saved.setGenres(new LinkedHashSet<>(genres));

        return saved;
    }

    @Override
    public Film update(Film film) {
        if (filmRepository.findById(film.getId()).isEmpty()) {
            throw new EntityNotFoundException("Film not found");
        }

        Film updated = filmRepository.update(film);

        // Перезаписываем жанры
        genreRepository.setGenresForFilm(updated.getId(), film.getGenres());

        Set<Genre> genres = genreRepository.getGenresByFilmId(updated.getId());
        updated.setGenres(new LinkedHashSet<>(genres));

        return updated;
    }

    @Override
    public void deleteById(Integer id) {
        boolean deleted = filmRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public Optional<Film> getById(Integer id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Film not found"));

        Set<Genre> genres = genreRepository.getGenresByFilmId(film.getId());
        film.setGenres(new LinkedHashSet<>(genres));

        return Optional.of(film);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmRepository.findAll();
    }
}