package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.model.Film.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;

public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        var releaseDate = rs.getDate("release_date");
        film.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());

        film.setDuration(rs.getInt("duration"));

        // В SQL нужен алиас: mr.name as mpa_name
        String mpaName = rs.getString("mpa_name");
        film.setRating(mpaName == null ? null : MpaRating.valueOf(mpaName.trim().toUpperCase()));

        // чтобы не ловить NPE — ставим пустой сет
        film.setGenres(new LinkedHashSet<>());

        return film;
    }
}
