package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User.User;
import ru.yandex.practicum.filmorate.storage.UserStorage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage.UserStorage;


import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    @Test
    void addFriend_shouldAddUsersToEachOtherFriends() {
        User user = createUser("user@mail.ru");
        User friend = createUser("friend@mail.ru");

        userService.addFriend(user.getId(), friend.getId());

        assertTrue(userService.getUserById(user.getId()).isFriend(friend.getId()));
        assertTrue(userService.getUserById(friend.getId()).isFriend(user.getId()));
    }

    @Test
    void removeFriend_shouldRemoveUsersFromEachOtherFriends() {
        User user = createUser("user@mail.ru");
        User friend = createUser("friend@mail.ru");

        userService.addFriend(user.getId(), friend.getId());
        userService.removeFriend(user.getId(), friend.getId());

        assertFalse(userService.getUserById(user.getId()).isFriend(friend.getId()));
        assertFalse(userService.getUserById(friend.getId()).isFriend(user.getId()));
    }

    @Test
    void getUserById_shouldReturnUser() {
        User user = createUser("user@mail.ru");

        User result = userService.getUserById(user.getId());

        assertEquals(user, result);
    }

    @Test
    void update_shouldUpdateUser() {
        User user = createUser("user@mail.ru");
        user.setName("Updated Name");

        User updated = userService.update(user);

        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void getFriendsById_shouldReturnFriends() {
        User user = createUser("user@mail.ru");
        User friend = createUser("friend@mail.ru");

        userService.addFriend(user.getId(), friend.getId());

        Collection<User> friends = userService.getFriendsById(user.getId());

        assertEquals(1, friends.size());
        assertTrue(friends.contains(friend));
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        User user1 = createUser("user1@mail.ru");
        User user2 = createUser("user2@mail.ru");
        User common = createUser("common@mail.ru");

        userService.addFriend(user1.getId(), common.getId());
        userService.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends =
                userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(common));
    }

    @Test
    void create_shouldCreateUser() {
        User user = new User();
        user.setEmail("new@mail.ru");
        user.setLogin("newLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userService.create(user);

        assertNotNull(created.getId());
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        createUser("first@mail.ru");
        createUser("second@mail.ru");

        Collection<User> users = userService.getAll();

        assertEquals(2, users.size());
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(email.split("@")[0]);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userService.create(user);
    }
}
