package ru.yandex.practicum.filmorate.storage.LikeStorage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.repository.repository.LikeRepository;

import java.util.Set;

@Component
@Primary
@RequiredArgsConstructor
public class DbLikeStorage implements LikeStorage {

    private final LikeRepository likeRepository;

    @Override
    public void addLike(Integer filmId, Integer userId) {
        likeRepository.addLike(filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        likeRepository.removeLike(filmId, userId);
    }

    @Override
    public boolean hasLike(Integer filmId, Integer userId) {
        return likeRepository.hasLike(filmId, userId);
    }

    @Override
    public int countLikes(Integer filmId) {
        return likeRepository.countLikes(filmId);
    }

    @Override
    public Set<Integer> getUserIdsByFilm(Integer filmId) {
        return likeRepository.getUserIdsByFilm(filmId);
    }
}
