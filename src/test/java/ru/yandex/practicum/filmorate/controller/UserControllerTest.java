package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
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
    void createUser_ShouldThrowValidateException_WhenEmailInvalid() {
        User user = createUserWithInvalidEmail();
        ValidateException exception = assertThrows(ValidateException.class,
                () -> userController.create(user));
        assertTrue(exception.getMessage().contains("Электронная почта не может быть пустой"));
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
    void updateUser_ShouldThrowValidateException_WhenUserNotFound() {
        User user = createValidUser("login", "Name", "test@example.com", LocalDate.of(2000, 1, 1));
        user.setId(999); // несуществующий ID
        ValidateException exception = assertThrows(ValidateException.class,
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
}


