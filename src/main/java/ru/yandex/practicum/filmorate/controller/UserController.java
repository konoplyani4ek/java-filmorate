package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> usersMap = new HashMap<>();
    private static int counter = 0;


    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        log.info("POST /films — попытка создать юзера: {}", newUser.getName());
        newUser.setId(generateId());
        validateNameField(newUser);
        usersMap.put(newUser.getId(), newUser);
        log.debug("Юзер создан: {}", newUser);
        return newUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("PUT /users — попытка обновить фильм с ID {}", newUser.getId());
        User existingUser = usersMap.get(newUser.getId());
        if (existingUser == null) {
            log.warn("Попытка обновить несуществующего юзера с ID {}", newUser.getId());
            throw new ValidateException("Пользователь с ID " + newUser.getId() + " не найден");
        }
        validateNameField(newUser);
        usersMap.put(newUser.getId(), newUser);
        log.debug("Юзер обновлён: {}", newUser);
        return newUser;
    }


    @GetMapping
    public Collection<User> getAll() {
        log.info("GET /users — попытка вернуть список юзеров, всего: {}", usersMap.size());
        return usersMap.values();
    }

    private int generateId() {
        return ++counter;
    }

    private void validateNameField(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя юзера взято из логина");
        }
    }
}
