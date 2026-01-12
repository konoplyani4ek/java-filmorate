package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.user.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.repository.repository.FriendRepository;
import ru.yandex.practicum.filmorate.repository.repository.UserRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Простые тесты для UserService.
 */
class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private FriendRepository friendRepository;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        friendRepository = mock(FriendRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);

        // Создаем сервис
        userService = new UserService(userRepository, friendRepository);
    }

    @Test
    void addFriend_shouldAddUsersToEachOtherFriends() {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("user@mail.ru");

        User friend = new User();
        friend.setId(2);
        friend.setEmail("friend@mail.ru");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findById(2)).thenReturn(Optional.of(friend));
        when(friendRepository.exists(1, 2)).thenReturn(false);

        // Act
        userService.addFriend(1, 2);

        // Assert
        verify(friendRepository, times(1)).add(1, 2, FriendshipStatus.CONFIRMED);
    }

    @Test
    void removeFriend_shouldRemoveUsersFromEachOtherFriends() {
        // Arrange
        User user = new User();
        user.setId(1);

        User friend = new User();
        friend.setId(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findById(2)).thenReturn(Optional.of(friend));

        // Act
        userService.removeFriend(1, 2);

        // Assert
        verify(friendRepository, times(1)).remove(1, 2);
    }

    @Test
    void getUserById_shouldReturnUser() {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("user@mail.ru");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("user@mail.ru", result.getEmail());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void update_shouldUpdateUser() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail("old@mail.ru");

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("user@mail.ru");
        updatedUser.setName("Updated Name");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.update(updatedUser);

        // Assert
        assertEquals("Updated Name", result.getName());
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    void getFriendsById_shouldReturnFriends() {
        // Arrange
        User user = new User();
        user.setId(1);

        User friend = new User();
        friend.setId(2);
        friend.setEmail("friend@mail.ru");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(friendRepository.findFriends(1)).thenReturn(Arrays.asList(friend));

        // Act
        Collection<User> friends = userService.getFriendsById(1);

        // Assert
        assertEquals(1, friends.size());
        assertTrue(friends.stream().anyMatch(f -> f.getId().equals(2)));
        verify(friendRepository, times(1)).findFriends(1);
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        // Arrange
        User user1 = new User();
        user1.setId(1);

        User user2 = new User();
        user2.setId(2);

        User common = new User();
        common.setId(3);
        common.setEmail("common@mail.ru");

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));
        when(friendRepository.getCommonFriends(1, 2)).thenReturn(Arrays.asList(common));

        // Act
        Collection<User> commonFriends = userService.getCommonFriends(1, 2);

        // Assert
        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.stream().anyMatch(f -> f.getId().equals(3)));
        verify(friendRepository, times(1)).getCommonFriends(1, 2);
    }

    @Test
    void create_shouldCreateUser() {
        // Arrange
        User user = new User();
        user.setEmail("new@mail.ru");
        user.setLogin("newLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = new User();
        createdUser.setId(1);
        createdUser.setEmail("new@mail.ru");
        createdUser.setLogin("newLogin");
        createdUser.setName("newLogin");
        createdUser.setBirthday(LocalDate.of(2000, 1, 1));

        when(userRepository.create(any(User.class))).thenReturn(createdUser);

        // Act
        User result = userService.create(user);

        // Assert
        assertNotNull(result.getId());
        assertEquals(1, result.getId());
        verify(userRepository, times(1)).create(any(User.class));
    }

    @Test
    void create_shouldSetNameFromLoginWhenNameIsEmpty() {
        // Arrange
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName(""); // Пустое имя
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = new User();
        createdUser.setId(1);
        createdUser.setEmail("test@mail.ru");
        createdUser.setLogin("testLogin");
        createdUser.setName("testLogin"); // Имя установлено из логина
        createdUser.setBirthday(LocalDate.of(2000, 1, 1));

        when(userRepository.create(any(User.class))).thenReturn(createdUser);

        // Act
        User result = userService.create(user);

        // Assert
        assertEquals("testLogin", result.getName());
        verify(userRepository, times(1)).create(any(User.class));
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1);
        user1.setEmail("first@mail.ru");

        User user2 = new User();
        user2.setId(2);
        user2.setEmail("second@mail.ru");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        Collection<User> users = userService.getAll();

        // Assert
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void addFriend_shouldThrowWhenUserNotFound() {
        // Arrange
        User user = new User();
        user.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userService.addFriend(1, 999);
        });
        verify(friendRepository, never()).add(anyInt(), anyInt(), any());
    }

    @Test
    void addFriend_shouldThrowWhenAlreadyFriends() {
        // Arrange
        User user = new User();
        user.setId(1);

        User friend = new User();
        friend.setId(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findById(2)).thenReturn(Optional.of(friend));
        when(friendRepository.exists(1, 2)).thenReturn(true); // Уже друзья

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.addFriend(1, 2);
        });
        verify(friendRepository, never()).add(anyInt(), anyInt(), any());
    }
}