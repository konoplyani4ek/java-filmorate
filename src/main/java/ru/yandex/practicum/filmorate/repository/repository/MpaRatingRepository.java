package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film.MpaRating;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class MpaRatingRepository {

    private final JdbcTemplate jdbc;

    public MpaRatingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int getOrCreateId(MpaRating rating) {
        if (rating == null) {
            throw new IllegalArgumentException("rating не должен быть null");
        }

        Optional<Integer> existing = findIdByName(rating.name());
        if (existing.isPresent()) {
            return existing.get();
        }

        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into mpa_rating(name) values (?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, rating.name());
            return ps;
        }, kh);

        Integer id = kh.getKeyAs(Integer.class);
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить mpa_rating");
        }
        return id;
    }

    public Optional<Integer> findIdByName(String name) {
        try {
            Integer id = jdbc.queryForObject(
                    "select mpa_rating_id from mpa_rating where name = ?",
                    Integer.class,
                    name
            );
            return Optional.ofNullable(id);
        } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
