package ru.yandex.practicum.filmorate.storage.likeStorage;

import java.util.Set;

public interface LikeStorage {

    void addLike(Integer filmId, Integer userId);

    void removeLike(Integer filmId, Integer userId);

    boolean hasLike(Integer filmId, Integer userId);

    int countLikes(Integer filmId);

    Set<Integer> getUserIdsByFilm(Integer filmId);
}

