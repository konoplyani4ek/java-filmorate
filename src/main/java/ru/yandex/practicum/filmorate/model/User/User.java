package ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private Integer id;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная должна содержать символ '@'")
    private String email;
    @NotBlank(message = "Логин не может быть пустым и содержать пробелы")
    private String login;
    private String name;
    @PastOrPresent
    private LocalDate birthday;
    @Setter(AccessLevel.NONE)
    private Set<Integer> friendIds = new HashSet<>();

    public void addFriend(int friendId) {
        if (friendId == this.id) {
            throw new IllegalArgumentException("Нельзя добавить себя в друзья");
        }
        if (friendIds.contains(friendId)) {
            throw new IllegalStateException("Пользователи уже друзья");
        }
        friendIds.add(friendId);
    }

    public void removeFriend(int friendId) {
        friendIds.remove(friendId);
    }

    public boolean isFriend(int friendId) {
        return friendIds.contains(friendId);
    }

    public Collection<Integer> getFriendIds() {
        return Collections.unmodifiableSet(friendIds);
    }
}
