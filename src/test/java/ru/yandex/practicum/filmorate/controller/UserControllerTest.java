package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User.User;
import ru.yandex.practicum.filmorate.repository.repository.FriendRepository;
import ru.yandex.practicum.filmorate.repository.repository.UserRepository;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты для UserController.
 * Используют моки вместо реальных Storage/Repository.
 */
class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private UserRepository userRepository;
    private FriendRepository friendRepository;
    private JdbcTemplate jdbcTemplate;

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    @BeforeEach
    void setUp() {
        // Создаем моки для зависимостей
        userRepository = Mockito.mock(UserRepository.class);
        friendRepository = Mockito.mock(FriendRepository.class);
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);

        // Создаем сервис с моками
        userService = new UserService(userRepository, friendRepository);

        // Создаем контроллер
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
        // Arrange
        User user = createValidUser("testuser", "Test Name", "test@example.com", LocalDate.of(2000, 1, 1));

        User createdUser = new User();
        createdUser.setId(1);
        createdUser.setLogin("testuser");
        createdUser.setName("Test Name");
        createdUser.setEmail("test@example.com");
        createdUser.setBirthday(LocalDate.of(2000, 1, 1));

        // Настройка мока: когда создаем пользователя, возвращаем его с ID
        when(userRepository.create(any(User.class))).thenReturn(createdUser);
        when(userRepository.findAll()).thenReturn(Arrays.asList(createdUser));

        // Act
        User created = userController.create(user);
        Collection<User> allUsers = userController.getAll();

        // Assert
        assertNotNull(created.getId());
        assertEquals("Test Name", created.getName());
        assertTrue(allUsers.contains(created));

        // Проверяем что метод create был вызван
        verify(userRepository, times(1)).create(any(User.class));
    }

    @Test
    void createUser_ShouldSetNameToLogin_WhenNameEmpty() {
        // Arrange
        User user = createUserWithEmptyName();

        User createdUser = new User();
        createdUser.setId(1);
        createdUser.setLogin("loginUser");
        createdUser.setName("loginUser"); // Имя установлено из логина
        createdUser.setEmail("test@example.com");
        createdUser.setBirthday(LocalDate.of(2000, 1, 1));

        when(userRepository.create(any(User.class))).thenReturn(createdUser);

        // Act
        User created = userController.create(user);

        // Assert
        assertEquals("loginUser", created.getName());
        verify(userRepository, times(1)).create(any(User.class));
    }

    @Test
    void createUser_ShouldFail_WhenEmailInvalid() {
        // Arrange
        User user = createUserWithInvalidEmail();

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации");

        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") &&
                        v.getMessage().contains("должна содержать символ '@'"));

        assertTrue(hasEmailError, "Email без '@' должен вызвать ошибку валидации");
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        // Arrange
        User existingUser = createValidUser("login", "Old Name", "test@example.com", LocalDate.of(2000, 1, 1));
        existingUser.setId(1);

        User updatedUser = createValidUser("newlogin", "New Name", "new@example.com", LocalDate.of(2001, 2, 2));
        updatedUser.setId(1);

        // Настройка моков
        when(userRepository.create(any(User.class))).thenReturn(existingUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);

        // Act
        User created = userController.create(existingUser);
        User updated = userController.update(updatedUser);

        // Assert
        assertEquals("New Name", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        // Arrange
        User user = createValidUser("login", "Name", "test@example.com", LocalDate.of(2000, 1, 1));
        user.setId(999); // несуществующий ID

        // Настройка мока: пользователь не найден
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userController.update(user));

        assertTrue(exception.getMessage().contains("не найден"));
        verify(userRepository, times(1)).findById(999);
        verify(userRepository, never()).update(any(User.class)); // update не должен вызываться
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        // Arrange
        User user1 = createValidUser("a", "A", "a@example.com", LocalDate.of(2000, 1, 1));
        user1.setId(1);

        User user2 = createValidUser("b", "B", "b@example.com", LocalDate.of(2001, 2, 2));
        user2.setId(2);

        List<User> users = Arrays.asList(user1, user2);

        // Настройка моков
        when(userRepository.create(any(User.class)))
                .thenReturn(user1)
                .thenReturn(user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        User created1 = userController.create(user1);
        User created2 = userController.create(user2);
        Collection<User> allUsers = userController.getAll();

        // Assert
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.contains(user1));
        assertTrue(allUsers.contains(user2));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void addFriend_ShouldThrow_WhenUserNotFound() {
        // Arrange
        int nonExistentUserId = 999;
        int existingUserId = 1;

        // Настройка моков: пользователи не найдены
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () ->
                userService.addFriend(nonExistentUserId, existingUserId)
        );

        assertThrows(EntityNotFoundException.class, () ->
                userService.addFriend(existingUserId, nonExistentUserId)
        );

        // Проверяем что friendRepository.add не вызывался
        verify(friendRepository, never()).add(anyInt(), anyInt(), any());
    }
}