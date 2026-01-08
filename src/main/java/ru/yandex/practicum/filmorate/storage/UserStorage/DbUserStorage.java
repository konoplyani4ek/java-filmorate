package ru.yandex.practicum.filmorate.storage.UserStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User.User;
import ru.yandex.practicum.filmorate.repository.repository.FriendRepository;
import ru.yandex.practicum.filmorate.repository.repository.UserRepository;

import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final JdbcTemplate jdbc;

    @Override
    public User create(User user) {
        return userRepository.create(user);
    }

    @Override
    public User update(User user) {
        Optional<User> existing = userRepository.findById(user.getId());
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        userRepository.update(user);
        return user;
    }

    @Override
    public void deleteById(Integer id) {
        boolean deleted = userRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public Optional<User> getById(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresent(u -> fillFriendIds(List.of(u)));
        return userOpt;
    }

    @Override
    public Collection<User> getFriendsById(Integer id) {
        List<User> friends = friendRepository.findFriends(id);
        fillFriendIds(friends);
        return friends;
    }

    @Override
    public Collection<User> getAll() {
        List<User> users = userRepository.findAll();
        fillFriendIds(users);
        return users;
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (userRepository.findById(userId).isEmpty() || userRepository.findById(friendId).isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        friendRepository.add(userId, friendId, FriendshipStatus.PENDING);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (userRepository.findById(userId).isEmpty() || userRepository.findById(friendId).isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        friendRepository.remove(userId, friendId);
    }

    public Collection<Integer> getFriends(Integer userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return friendRepository.findFriendIds(userId);
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        if (userRepository.findById(userId).isEmpty() || userRepository.findById(otherId).isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return friendRepository.getCommonFriends(userId, otherId);
    }

    private void fillFriendIds(List<User> users) {

        if (users == null || users.isEmpty()) {
            return;
        }

        // Собираем ID всех пользователей
        List<Integer> userIds = users.stream()
                .filter(user -> user.getId() != null)
                .map(User::getId)
                .toList();

        if (userIds.isEmpty()) {
            return;
        }

        // Создаём Map для быстрого поиска пользователя по ID
        Map<Integer, User> userMap = new HashMap<>();
        for (User user : users) {
            if (user.getId() != null) {
                userMap.put(user.getId(), user);
            }
        }

        // Загружаем ВСЕХ друзей одним запросом
        String sql = "SELECT user_id, friend_id FROM friends WHERE user_id IN (" +
                createPlaceholders(userIds.size()) + ")";

        // Для каждой строки результата добавляем друга пользователю
        jdbc.query(sql, rs -> {
            int userId = rs.getInt("user_id");
            int friendId = rs.getInt("friend_id");

            User user = userMap.get(userId);
            if (user != null && !user.isFriend(friendId)) {
                user.addFriend(friendId);
            }
        }, userIds.toArray());
    }

    private String createPlaceholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }
}