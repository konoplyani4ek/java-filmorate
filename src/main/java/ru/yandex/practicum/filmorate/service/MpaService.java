package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.repository.repository.MpaRatingRepository;


import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {

    private final MpaRatingRepository mpaRatingRepository;

    public Collection<MpaRating> getAllMpa() {
        log.info("Получение всех рейтингов MPA");
        Collection<MpaRating> ratings = mpaRatingRepository.findAll();
        log.debug("Найдено рейтингов MPA: {}", ratings.size());
        return ratings;
    }

    public MpaRating getMpaById(Integer id) {
        log.info("Получение рейтинга MPA с id={}", id);

        if (!mpaRatingRepository.existsById(id)) {
            log.warn("Рейтинг MPA с id={} не найден", id);
            throw new EntityNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }

        MpaRating rating = mpaRatingRepository.findById(id);
        log.debug("Найден рейтинг MPA: {}", rating.getName());
        return rating;
    }
}