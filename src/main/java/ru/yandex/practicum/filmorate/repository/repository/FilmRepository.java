package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.model.Film.Genre;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {

    private final GenreRepository genreRepository;
    private final MpaRatingRepository mpaRatingRepository;

    public FilmRepository(JdbcTemplate jdbc,
                          GenreRepository genreRepository,
                          MpaRatingRepository mpaRatingRepository) {
        super(jdbc, new FilmRowMapper());
        this.genreRepository = genreRepository;
        this.mpaRatingRepository = mpaRatingRepository;
    }

    public Film create(Film film) {
        int ratingId = mpaRatingRepository.getOrCreateId(film.getRating());
        long id = insert(
                "insert into films(name, description, release_date, duration, mpa_rating_id) values (?, ?, ?, ?, ?)",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                ratingId
        );
        film.setId((int) id);
        genreRepository.setGenresForFilm(film.getId(), film.getGenres());
        return film;
    }

    public Film update(Film film) {
        int ratingId = mpaRatingRepository.getOrCreateId(film.getRating());
        update(
                "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? where film_id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                ratingId,
                film.getId()
        );
        genreRepository.setGenresForFilm(film.getId(), film.getGenres());
        return film;
    }

    public boolean deleteById(int id) {
        return delete("delete from films where film_id = ?", id);
    }

    public Optional<Film> findById(int id) {
        Optional<Film> filmOpt = findOne(
                "select f.film_id, f.name, f.description, f.release_date, f.duration, mr.name as mpa_name " +
                        "from films f " +
                        "join mpa_rating mr on mr.mpa_rating_id = f.mpa_rating_id " +
                        "where f.film_id = ?",
                id
        );
        filmOpt.ifPresent(f -> {
            Set<Genre> genres = genreRepository.getGenresByFilmId(f.getId());
            f.setGenres(new LinkedHashSet<>(genres));  // Convert to LinkedHashSet
        });
        return filmOpt;
    }

    public List<Film> findAll() {
        List<Film> films = findMany(
                "select f.film_id, f.name, f.description, f.release_date, f.duration, mr.name as mpa_name " +
                        "from films f " +
                        "join mpa_rating mr on mr.mpa_rating_id = f.mpa_rating_id"
        );

        if (films.isEmpty()) {
            return films;
        }

        Set<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toSet());
        Map<Integer, Set<Genre>> genresByFilm = genreRepository.getGenresByFilmIds(filmIds);

        for (Film f : films) {
            Set<Genre> genres = genresByFilm.getOrDefault(f.getId(), Set.of());
            f.setGenres(new LinkedHashSet<>(genres));  // Convert to LinkedHashSet
        }
        return films;
    }
}