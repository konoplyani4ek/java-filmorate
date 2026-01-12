package ru.yandex.practicum.filmorate.storage.likeStorage;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class InMemoryLikeStorage implements LikeStorage {

    private final Map<Integer, Set<Integer>> likes = new HashMap<>();

    @Override
    public void addLike(Integer filmId, Integer userId) {
        Set<Integer> users = likes.get(filmId);
        if (users == null) {
            users = new HashSet<>();
            likes.put(filmId, users);
        }
        users.add(userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        Set<Integer> users = likes.get(filmId);
        if (users != null) {
            users.remove(userId);
        }
    }

    @Override
    public boolean hasLike(Integer filmId, Integer userId) {
        return likes.getOrDefault(filmId, Set.of()).contains(userId);
    }

    @Override
    public int countLikes(Integer filmId) {
        return likes.getOrDefault(filmId, Set.of()).size();
    }

    @Override
    public Set<Integer> getUserIdsByFilm(Integer filmId) {
        return Set.copyOf(likes.getOrDefault(filmId, Set.of()));
    }
}

