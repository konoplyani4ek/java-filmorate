package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {

    private final GenreRepository genreRepository;
    private final MpaRatingRepository mpaRatingRepository;

    public FilmRepository(JdbcTemplate jdbc,
                          FilmRowMapper filmRowMapper,
                          GenreRepository genreRepository,
                          MpaRatingRepository mpaRatingRepository) {
        super(jdbc, filmRowMapper);
        this.genreRepository = genreRepository;
        this.mpaRatingRepository = mpaRatingRepository;
    }

    public Film create(Film film) {
        //Получаем ID из MpaRating объекта
        Integer ratingId = null;
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            ratingId = film.getMpa().getId();
        }

        long id = insert(
                "INSERT INTO films(name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                ratingId
        );
        film.setId((int) id);

        // Сохранение жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreRepository.setGenresForFilm(film.getId(), film.getGenres());
        }

        return film;
    }

    public Film update(Film film) {
        //  Получаем ID из MpaRating объекта
        Integer ratingId = null;
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            ratingId = film.getMpa().getId();
        }

        update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                ratingId,
                film.getId()
        );

        // Обновление жанров
        genreRepository.setGenresForFilm(film.getId(), film.getGenres());

        return film;
    }

    public boolean deleteById(int id) {
        return delete("DELETE FROM films WHERE film_id = ?", id);
    }

    public Optional<Film> findById(int id) {
        // Загружаем полные данные MPA
        Optional<Film> filmOpt = findOne(
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                        "       mr.rating_id, mr.name as mpa_name, mr.description as mpa_description " +
                        "FROM films f " +
                        "LEFT JOIN mpa_ratings mr ON mr.rating_id = f.rating_id " +
                        "WHERE f.film_id = ?",
                id
        );

        filmOpt.ifPresent(f -> {
            // Загрузка жанров
            Set<Genre> genres = genreRepository.getGenresByFilmId(f.getId());
            f.setGenres(new LinkedHashSet<>(genres));
        });

        return filmOpt;
    }

    public List<Film> findAll() {
        //  Загружаем полные данные MPA
        List<Film> films = findMany(
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                        "       mr.rating_id, mr.name as mpa_name, mr.description as mpa_description " +
                        "FROM films f " +
                        "LEFT JOIN mpa_ratings mr ON mr.rating_id = f.rating_id"
        );

        if (films.isEmpty()) {
            return films;
        }

        // Загрузка жанров для всех фильмов
        Set<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());
        Map<Integer, Set<Genre>> genresByFilm = genreRepository.getGenresByFilmIds(filmIds);

        for (Film f : films) {
            Set<Genre> genres = genresByFilm.getOrDefault(f.getId(), Set.of());
            f.setGenres(new LinkedHashSet<>(genres));
        }

        return films;
    }
}