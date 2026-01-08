package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class UserControllerTest {

    private UserController userController;
    private UserStorage userStorage;
    private UserService userService;
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(new InMemoryUserStorage());
        userController = new UserController(userService);
    }

    private User createValidUser(String login, String name, String email, LocalDate birthday) {
        User user = new User();
        user.setLogin(login);
        user.setName(name);
        user.setEmail(email);
        user.setBirthday(birthday);
        return user;
    }

    private User createUserWithEmptyName() {
        return createValidUser("loginUser", "", "test@example.com", LocalDate.of(2000, 1, 1));
    }

    private User createUserWithInvalidEmail() {
        return createValidUser("testuser", "Name", "invalid-email", LocalDate.of(2000, 1, 1));
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        User user = createValidUser("testuser", "Test Name", "test@example.com", LocalDate.of(2000, 1, 1));
        User created = userController.create(user);
        assertNotNull(created.getId());
        assertEquals("Test Name", created.getName());
        assertTrue(userController.getAll().contains(created));
    }

    @Test
    void createUser_ShouldSetNameToLogin_WhenNameEmpty() {
        User user = createUserWithEmptyName();
        User created = userController.create(user);
        assertEquals("loginUser", created.getName());
    }

    @Test
    void createUser_ShouldFail_WhenEmailInvalid() {
        User user = createUserWithInvalidEmail();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации");

        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") &&
                        v.getMessage().contains("должна содержать символ '@'"));

        assertTrue(hasEmailError, "Email без '@' должен вызвать ошибку валидации");
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        User created = userController.create(createValidUser("login", "Old Name", "test@example.com", LocalDate.of(2000, 1, 1)));
        User updatedUser = createValidUser("newlogin", "New Name", "new@example.com", LocalDate.of(2001, 2, 2));
        updatedUser.setId(created.getId());
        User updated = userController.update(updatedUser);
        assertEquals("New Name", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void updateUser_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        User user = createValidUser("login", "Name", "test@example.com", LocalDate.of(2000, 1, 1));
        user.setId(999); // несуществующий ID
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userController.update(user));
        assertTrue(exception.getMessage().contains("не найден"));
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        User user1 = userController.create(createValidUser("a", "A", "a@example.com", LocalDate.of(2000, 1, 1)));
        User user2 = userController.create(createValidUser("b", "B", "b@example.com", LocalDate.of(2001, 2, 2)));
        Collection<User> allUsers = userController.getAll();
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.contains(user1));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    void addFriend_ShouldThrow_WhenUserNotFound() {
        int nonExistentUserId = 999;
        int existingUserId = 1;

        assertThrows(EntityNotFoundException.class, () ->
                userService.addFriend(nonExistentUserId, existingUserId)
        );

        assertThrows(EntityNotFoundException.class, () ->
                userService.addFriend(existingUserId, nonExistentUserId)
        );
    }

}


