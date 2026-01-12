package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRatingRowMapper;

import java.util.List;

@Repository
public class MpaRatingRepository {

    private final JdbcTemplate jdbc;
    private final MpaRatingRowMapper mpaMapper;

    public MpaRatingRepository(JdbcTemplate jdbc, MpaRatingRowMapper mpaMapper) {
        this.jdbc = jdbc;
        this.mpaMapper = mpaMapper;
    }

    public boolean existsById(Integer id) {
        if (id == null) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE rating_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public MpaRating findById(Integer id) {
        String sql = "SELECT rating_id, name, description FROM mpa_ratings WHERE rating_id = ?";
        return jdbc.queryForObject(sql, mpaMapper, id);
    }

    public List<MpaRating> findAll() {
        String sql = "SELECT rating_id, name, description FROM mpa_ratings ORDER BY rating_id";
        return jdbc.query(sql, mpaMapper);
    }
}