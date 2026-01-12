package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.user.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.repository.repository.FriendRepository;
import ru.yandex.practicum.filmorate.repository.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public User create(User user) {
        log.info("Попытка создать пользователя: {}", user.getLogin());
        setNameByLoginIfEmpty(user);
        User created = userRepository.create(user);
        log.debug("Пользователь создан с ID={}", created.getId());
        return created;
    }

    @Transactional
    public User update(User user) {
        log.info("Обновление пользователя с ID={}", user.getId());

        userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + user.getId() + " не найден"));

        setNameByLoginIfEmpty(user);
        User updated = userRepository.update(user);
        log.debug("Пользователь с ID {} обновлен", user.getId());
        return updated;
    }

    public Collection<User> getAll() {
        log.info("Получение всех пользователей");
        return userRepository.findAll();
    }

    public User getUserById(Integer id) {
        log.info("Получение пользователя с ID {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + id + " не найден"));
    }

    @Transactional
    public void deleteUser(Integer id) {
        log.info("Удаление пользователя с ID={}", id);

        // Проверка существования
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + id + " не найден"));

        boolean deleted = userRepository.deleteById(id);

        if (!deleted) {
            throw new EntityNotFoundException("Не удалось удалить пользователя с id " + id);
        }

        log.info("Пользователь с ID={} удален", id);
    }

    @Transactional
    public void addFriend(Integer userId, Integer friendId) {
        log.info("Пользователь {} добавляет в друзья пользователя {}", userId, friendId);

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));
        userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + friendId + " не найден"));

        if (friendRepository.exists(userId, friendId)) {
            log.warn("Попытка повторно добавить в друзья: userId={}, friendId={}", userId, friendId);
            throw new IllegalArgumentException("Пользователи уже друзья");
        }

        // Добавление дружбы со статусом CONFIRMED
        friendRepository.add(userId, friendId, FriendshipStatus.CONFIRMED);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    @Transactional
    public void removeFriend(Integer userId, Integer friendId) {
        log.info("Пользователь {} удаляет из друзей пользователя {}", userId, friendId);

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));
        userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + friendId + " не найден"));

        friendRepository.remove(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public Collection<User> getFriendsById(Integer id) {
        log.info("Получение друзей пользователя с ID {}", id);

        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + id + " не найден"));

        List<User> friends = friendRepository.findFriends(id);
        log.debug("У пользователя {} найдено {} друзей", id, friends.size());
        return friends;
    }

    public Collection<User> getCommonFriends(Integer userId, Integer anotherId) {
        log.info("Получение общих друзей для пользователей {} и {}", userId, anotherId);

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));
        userRepository.findById(anotherId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + anotherId + " не найден"));

        List<User> commonFriends = friendRepository.getCommonFriends(userId, anotherId);

        log.debug("Найдено {} общих друзей для пользователей {} и {}",
                commonFriends.size(), userId, anotherId);
        return commonFriends;
    }

    private void setNameByLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено из логина: {}", user.getLogin());
        }
    }
}