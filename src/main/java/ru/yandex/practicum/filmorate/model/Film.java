package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import ru.yandex.practicum.filmorate.validation.ReleaseDateAfter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {

    private Integer id;
    @NotBlank
    private String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;
    @ReleaseDateAfter
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
    @Setter(AccessLevel.NONE)
    private Set<Integer> likedByUserId = new HashSet<>();

    public void addLike(int userId) {
        if (!likedByUserId.add(userId)) {
            throw new IllegalStateException("Пользователь уже лайкнул фильм");
        }
    }

    public void removeLike(int userId) {
        likedByUserId.remove(userId);
    }

    public Collection<Integer> getLikedByUserId() {
        return Collections.unmodifiableCollection(likedByUserId);
    }


}