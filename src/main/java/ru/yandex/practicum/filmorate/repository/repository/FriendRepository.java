package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User.FriendshipStatus;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Repository
public class FriendRepository {

    private final JdbcTemplate jdbc;
    private final FriendshipStatusRepository statusRepository;

    public FriendRepository(JdbcTemplate jdbc, FriendshipStatusRepository statusRepository) {
        this.jdbc = jdbc;
        this.statusRepository = statusRepository;
    }

    public boolean exists(int userId, int friendId) {
        Integer cnt = jdbc.queryForObject(
                "select count(*) from friends where user_id = ? and friend_id = ?",
                Integer.class,
                userId, friendId
        );
        return cnt != null && cnt > 0;
    }

    public void add(int userId, int friendId, FriendshipStatus status) {
        int statusId = statusRepository.getOrCreateId(status);
        jdbc.update(
                "insert into friends(user_id, friend_id, status_id) values (?, ?, ?)",
                userId, friendId, statusId
        );
    }

    public void remove(int userId, int friendId) {
        jdbc.update("delete from friends where user_id = ? and friend_id = ?", userId, friendId);
    }

    public Collection<Integer> findFriendIds(int userId) {
        List<Integer> ids = jdbc.queryForList(
                "select friend_id from friends where user_id = ?",
                Integer.class,
                userId
        );
        return new HashSet<>(ids);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        return jdbc.query("""
                    SELECT u.*
                    FROM users u
                    JOIN friends f1 ON u.user_id = f1.friend_id
                    JOIN friends f2 ON u.user_id = f2.friend_id
                    WHERE f1.user_id = ? AND f2.user_id = ?
                """, new UserRowMapper(), userId, otherId);
    }

    /**
     * Возвращает пользователей-друзей (без заполнения их friendIds).
     * friendIds подгружаются отдельно в DbUserStorage.
     */
    public List<User> findFriends(int userId) {
        return jdbc.query(
                "select u.* " +
                        "from friends f " +
                        "join users u on u.user_id = f.friend_id " +
                        "where f.user_id = ?",
                new UserRowMapper(),
                userId
        );
    }
}
