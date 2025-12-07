package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> usersMap = new HashMap<>();
    private static int counter = 0;


    @PostMapping
    public User create(@RequestBody User newUser) {
        log.info("POST /films — попытка создать юзера: {}", newUser.getName());
        validateUser(newUser);
        newUser.setId(generateId());
        usersMap.put(newUser.getId(), newUser);
        log.debug("Юзер создан: {}", newUser);
        return newUser;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("PUT /users — попытка обновить фильм с ID {}", newUser.getId());
        User existingUser = usersMap.get(newUser.getId());
        if (existingUser == null) {
            log.warn("Попытка обновить несуществующего юзера с ID {}", newUser.getId());
            throw new ValidateException("Пользователь с ID " + newUser.getId() + " не найден");
        }
        validateUser(newUser);
        usersMap.put(newUser.getId(), newUser);
        log.debug("Юзер обновлён: {}", newUser);
        return newUser;
    }


    @GetMapping
    public Collection<User> getAll() {
        log.info("GET /users — попытка вернуть список юзеров, всего: {}", usersMap.size());
        return usersMap.values();
    }

    private void validateUser(User user) {
        List<String> errors = Stream.of(
                        (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@"))
                                ? "Электронная почта не может быть пустой и должна содержать символ '@'" : null,
                        (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" "))
                                ? "Логин не может быть пустым и содержать пробелы" : null,
                        (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now()))
                                ? "Дата рождения не может быть в будущем" : null
                )
                .filter(Objects::nonNull)
                .toList();
        if (!errors.isEmpty()) {
            String errorMsg = String.join("; ", errors);
            log.warn("Ошибка валидации фильма {}: {}", user.getName(), errorMsg);
            throw new ValidateException(errorMsg);
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private int generateId() {
        return ++counter;
    }

}
