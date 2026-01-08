package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User.FriendshipStatus;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class FriendshipStatusRepository {

    private final JdbcTemplate jdbc;

    public FriendshipStatusRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int getOrCreateId(FriendshipStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status не должен быть null");
        }

        Optional<Integer> existing = findIdByName(status.name());
        if (existing.isPresent()) {
            return existing.get();
        }

        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into friendship_status(name) values (?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, status.name());
            return ps;
        }, kh);

        Integer id = kh.getKeyAs(Integer.class);
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить friendship_status");
        }
        return id;
    }

    public Optional<Integer> findIdByName(String name) {
        try {
            Integer id = jdbc.queryForObject(
                    "select status_id from friendship_status where name = ?",
                    Integer.class,
                    name
            );
            return Optional.ofNullable(id);
        } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
