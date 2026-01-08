package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GenreRowMapper implements RowMapper<Genre> {

    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        // лучше в SQL выбирать g.name as genre_name
        String name = rs.getString("genre_name");
        if (name == null) {
            name = rs.getString("name");
        }
        return name == null ? null : Genre.valueOf(name.trim().toUpperCase());
    }
}
