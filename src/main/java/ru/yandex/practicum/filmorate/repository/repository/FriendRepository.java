package ru.yandex.practicum.filmorate.repository.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.user.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FriendRepository {

    private final JdbcTemplate jdbc;

    // Проверить существование связи дружбы
    public boolean exists(int userId, int friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    public void add(int userId, int friendId, FriendshipStatus status) {
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";

        // Преобразуем enum в строку для хранения в БД
        String statusString = status.name(); // "PENDING" или "CONFIRMED"

        log.debug("Adding friend: userId={}, friendId={}, status={}", userId, friendId, statusString);
        jdbc.update(sql, userId, friendId, statusString);
    }

    public void remove(int userId, int friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        log.debug("Removing friend: userId={}, friendId={}", userId, friendId);
        jdbc.update(sql, userId, friendId);
    }

    public Collection<Integer> findFriendIds(int userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<Integer> ids = jdbc.queryForList(sql, Integer.class, userId);
        return new HashSet<>(ids);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = """
                SELECT u.*
                FROM users u
                JOIN friends f1 ON u.user_id = f1.friend_id
                JOIN friends f2 ON u.user_id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """;

        log.debug("Getting common friends for users: {} and {}", userId, otherId);
        return jdbc.query(sql, new UserRowMapper(), userId, otherId);
    }

    // Возвращает пользователей-друзей без заполнения их friendIds
    public List<User> findFriends(int userId) {
        String sql = """
                SELECT u.*
                FROM friends f
                JOIN users u ON u.user_id = f.friend_id
                WHERE f.user_id = ?
                """;

        log.debug("Getting friends for user: {}", userId);
        return jdbc.query(sql, new UserRowMapper(), userId);
    }

    // return FriendshipStatus или null если связи нет
    public FriendshipStatus getStatus(int userId, int friendId) {
        String sql = "SELECT status FROM friends WHERE user_id = ? AND friend_id = ?";

        try {
            String statusString = jdbc.queryForObject(sql, String.class, userId, friendId);

            // Преобразуем строку из БД в enum
            return FriendshipStatus.valueOf(statusString);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.debug("No friendship found between users {} and {}", userId, friendId);
            return null;
        }
    }

    public void updateStatus(int userId, int friendId, FriendshipStatus status) {
        String sql = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
        String statusString = status.name();

        log.debug("Updating friendship status: userId={}, friendId={}, newStatus={}",
                userId, friendId, statusString);
        jdbc.update(sql, statusString, userId, friendId);
    }

    public void confirmFriendship(int userId, int friendId) {
        updateStatus(userId, friendId, FriendshipStatus.CONFIRMED);
    }
}