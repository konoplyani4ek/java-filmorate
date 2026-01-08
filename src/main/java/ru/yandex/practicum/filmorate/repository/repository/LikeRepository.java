package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class LikeRepository {

    private final JdbcTemplate jdbc;

    public LikeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void addLike(int filmId, int userId) {
        jdbc.update(
                "insert into user_likes(film_id, user_id) values (?, ?)",
                filmId, userId
        );
    }

    public void removeLike(int filmId, int userId) {
        jdbc.update(
                "delete from user_likes where film_id = ? and user_id = ?",
                filmId, userId
        );
    }

    public boolean hasLike(int filmId, int userId) {
        Integer cnt = jdbc.queryForObject(
                "select count(*) from user_likes where film_id = ? and user_id = ?",
                Integer.class,
                filmId, userId
        );
        return cnt != null && cnt > 0;
    }

    public int countLikes(int filmId) {
        Integer cnt = jdbc.queryForObject(
                "select count(*) from user_likes where film_id = ?",
                Integer.class,
                filmId
        );
        return cnt == null ? 0 : cnt;
    }

    public Set<Integer> getUserIdsByFilm(int filmId) {
        List<Integer> ids = jdbc.queryForList(
                "select user_id from user_likes where film_id = ?",
                Integer.class,
                filmId
        );
        return new HashSet<>(ids);
    }
}
