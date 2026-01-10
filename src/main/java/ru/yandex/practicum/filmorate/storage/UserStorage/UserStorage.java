package ru.yandex.practicum.filmorate.storage.UserStorage;

import ru.yandex.practicum.filmorate.model.User.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    User update(User user);

    void deleteById(Integer id);

    Optional<User> getById(Integer id);

    Collection<User> getFriendsById(Integer id);

    Collection<User> getAll();

}
