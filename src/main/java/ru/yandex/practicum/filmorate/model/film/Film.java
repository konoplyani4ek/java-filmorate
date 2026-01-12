package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ReleaseDateAfter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
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

    @JsonProperty("mpa")
    private MpaRating mpa;

    public void addGenre(Genre genre) {
        if (genres == null) {
            genres = new LinkedHashSet<>();
        }
        genres.add(genre);
    }

    public void removeGenre(Genre genre) {
        if (genres != null) {
            genres.remove(genre);
        }
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres != null ? new LinkedHashSet<>(genres) : new LinkedHashSet<>();
    }

    public Set<Genre> getGenres() {
        return genres != null ? genres : new LinkedHashSet<>();
    }
}