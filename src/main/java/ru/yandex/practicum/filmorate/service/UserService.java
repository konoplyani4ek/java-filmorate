package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Integer userId, Integer friendId) {
        User user = getByIdOrThrowNotFound(userId);
        if (user.isFriend(friendId)) {
            log.warn(
                    "Попытка повторно добавить в друзья: userId={}, friendId={}",
                    userId, friendId);
            throw new IllegalArgumentException("Пользователи уже друзья");
        }
        User friendUser = getByIdOrThrowNotFound(friendId);
        user.addFriend(friendId);
        friendUser.addFriend(userId);
        log.info("Пользователи с ID {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {

        User user = getByIdOrThrowNotFound(userId);

        User friendUser = getByIdOrThrowNotFound(friendId);

        user.removeFriend(friendId);
        friendUser.removeFriend(userId);

        log.info("Пользователи с ID {} и {} больше не друзья", userId, friendId);
    }

    public User getUserById(Integer id) {
        return getByIdOrThrowNotFound(id);
    }

    public User update(User user) {
        log.info("Обновляю юзера с id={}", user.getId());
        getUserById(user.getId());
        log.debug("User updated: id={}", user.getId());
        return userStorage.update(user);
    }

    public Collection<User> getFriendsById(Integer id) {
        getByIdOrThrowNotFound(id);
        log.info("Друзья пользователя {}: {}", id, userStorage.getFriendsById(id));
        return userStorage.getFriendsById(id); //может быть пустой
    }

    public Collection<User> getCommonFriends(Integer id, Integer anotherId) {
        log.info("Получение списка общих друзей для пользователей {} и {}", id, anotherId);
        Collection<User> firstUserFriends = getFriendsById(id);
        Collection<User> anotherUserFriends = getFriendsById(anotherId);
        if (firstUserFriends.isEmpty() || anotherUserFriends.isEmpty()) {
            log.info("У юзеров с id {} и {} нет общих друзей", id, anotherId);
            return Collections.emptyList();
        }
        return firstUserFriends.stream().filter(anotherUserFriends::contains).collect(Collectors.toList());
    }

    public User create(User newUser) {
        log.info("попытка создать юзера: {}", newUser.getName());
        userStorage.create(newUser);
        setNameByLoginIfEmpty(newUser);
        log.debug("Юзер создан: {}", newUser);
        return newUser;
    }

    public Collection<User> getAll() {
        log.info("вывод всех юзеров");
        return userStorage.getAll();
    }

    private User getByIdOrThrowNotFound(Integer id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + id + " не найден"));
    }

    private void setNameByLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя юзера взято из логина");
        }
    }
}
