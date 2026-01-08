package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film.MpaRating;

import java.util.*;

@Service
@Slf4j
public class MpaService {

    public Collection<MpaRating> getAllMpa() {
        log.info("Получение всех рейтингов MPA");
        List<MpaRating> ratings = Arrays.asList(MpaRating.values());
        log.debug("Найдено рейтингов MPA: {}", ratings.size());
        return ratings;
    }

    public MpaRating getMpaById(Integer id) {
        log.info("Получение рейтинга MPA с id={}", id);
        if (id < 1 || id > MpaRating.values().length) {
            log.warn("Рейтинг MPA с id={} не найден", id);
            throw new EntityNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        MpaRating rating = MpaRating.values()[id - 1];
        log.debug("Найден рейтинг MPA: {}", rating.getDisplayName());
        return rating;
    }
}