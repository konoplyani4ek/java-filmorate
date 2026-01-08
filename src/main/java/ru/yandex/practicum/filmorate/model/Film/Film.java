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
    private LinkedHashSet<Genre> genres;
    private MpaRating rating;

    @JsonGetter("mpa")
    public MpaRating getMpa() {
        return rating;
    }

    @JsonSetter("mpa")
    public void setMpa(Map<String, Integer> mpa) {
        if (mpa == null || !mpa.containsKey("id")) {
            this.rating = null;
            return;
        }
        int id = mpa.get("id");
        if (id < 1 || id > MpaRating.values().length) {
            throw new EntityNotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
        this.rating = MpaRating.values()[id - 1];
    }


}