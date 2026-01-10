package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.model.Film.MpaRating;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;

@Component
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

        // ✅ ИЗМЕНЕНО: Загрузка MpaRating как объекта
        Integer ratingId = (Integer) rs.getObject("rating_id");
        if (ratingId != null) {
            MpaRating mpa = new MpaRating();
            mpa.setId(ratingId);

            // Загружаем name если есть в SELECT
            try {
                String mpaName = rs.getString("mpa_name");
                if (mpaName != null) {
                    mpa.setName(mpaName);
                }
            } catch (SQLException ignored) {
                // Колонка может отсутствовать
            }

            // Загружаем description если есть в SELECT
            try {
                String mpaDescription = rs.getString("mpa_description");
                if (mpaDescription != null) {
                    mpa.setDescription(mpaDescription);
                }
            } catch (SQLException ignored) {
                // Колонка может отсутствовать
            }

            film.setMpa(mpa);
        }

        // Чтобы не ловить NPE — ставим пустой set
        film.setGenres(new LinkedHashSet<>());

        return film;
    }
}