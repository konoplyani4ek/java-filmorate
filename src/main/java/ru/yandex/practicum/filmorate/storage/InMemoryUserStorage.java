package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> usersMap = new HashMap<>();
    private static int counter = 0;

    @Override
    public User create(User user) {
        user.setId(generateId());
        setNameByLoginIfEmpty(user);
        usersMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        usersMap.put(user.getId(), user);
        return user;
    }


    @Override // дописать логи?
    public void deleteById(Integer id) {
        User removed = usersMap.remove(id);
        if (removed == null) {
            throw new NoSuchElementException("Юзер с id=" + id + " не найден");
        }
    }

    private int generateId() {
        return ++counter;
    }

    private void setNameByLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя юзера взято из логина");
        }
    }

    public Optional<User> getById(Integer id) {
        User user = usersMap.get(id);
        return Optional.ofNullable(user);
    }

    public Collection<User> getFriendsById(Integer id) {
        User user = usersMap.get(id);
        if (user == null) {
            return Collections.emptyList();
        }

        return user.getFriendIds().stream()
                .map(usersMap::get) //превращает стрим из айди в юзеров
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Collection<User> getAll() {
        return usersMap.values();
    }

}
