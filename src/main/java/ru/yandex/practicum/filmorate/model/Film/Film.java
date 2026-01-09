package ru.yandex.practicum.filmorate.model.Film;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.validation.ReleaseDateAfter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Фильм.
 */
@Data
public class Film {

    private Integer id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @ReleaseDateAfter
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;


    private Set<Genre> genres = new LinkedHashSet<>();


    private MpaRating rating;


    @JsonGetter("mpa")
    public MpaRating getMpa() {
        return rating;
    }

    /**
     * Setter для JSON десериализации MPA рейтинга.
     * Принимает объект вида {"id": 1} или {"id": 1, "name": "G"}
     */
    @JsonSetter("mpa")
    public void setMpa(Map<String, Object> mpa) {
        if (mpa == null || !mpa.containsKey("id")) {
            this.rating = null;
            return;
        }

        // Получаем id из Map (может быть Integer или String)
        Object idObj = mpa.get("id");
        int id;
        if (idObj instanceof Integer) {
            id = (Integer) idObj;
        } else if (idObj instanceof String) {
            id = Integer.parseInt((String) idObj);
        } else {
            throw new IllegalArgumentException("MPA id должен быть числом");
        }

        // Валидация диапазона
        if (id < 1 || id > MpaRating.values().length) {
            throw new EntityNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }

        // Получаем enum по индексу (id - 1, т.к. enum начинается с 0)
        this.rating = MpaRating.values()[id - 1];
    }

    /**
     * Прямой setter для внутреннего использования.
     */
    public void setRating(MpaRating rating) {
        this.rating = rating;
    }

    /**
     * Добавить жанр к фильму.
     */
    public void addGenre(Genre genre) {
        if (genres == null) {
            genres = new LinkedHashSet<>();
        }
        genres.add(genre);
    }

    /**
     * Удалить жанр из фильма.
     */
    public void removeGenre(Genre genre) {
        if (genres != null) {
            genres.remove(genre);
        }
    }

    /**
     * Установить жанры фильма.
     */
    public void setGenres(Set<Genre> genres) {
        this.genres = genres != null ? new LinkedHashSet<>(genres) : new LinkedHashSet<>();
    }

    /**
     * Получить жанры фильма.
     */
    public Set<Genre> getGenres() {
        return genres != null ? genres : new LinkedHashSet<>();
    }
}