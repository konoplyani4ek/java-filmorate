package ru.yandex.practicum.filmorate.model.Film;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;

import java.util.Map;

@Getter
public enum Genre {

    COMEDY("Комедия"),
    DRAMA("Драма"),
    CARTOON("Мультфильм"),
    THRILLER("Триллер"),
    DOCUMENTARY("Документальный"),
    ACTION("Боевик");

    private final String russianName;

    Genre(String russianName) {
        this.russianName = russianName;
    }

    @JsonCreator
    public static Genre fromJson(Map<String, Integer> json) {
        if (json == null || !json.containsKey("id")) {
            return null;
        }
        int id = json.get("id");
        if (id < 1 || id > Genre.values().length) {
            throw new EntityNotFoundException("Жанр с id " + id + " не найден");
        }
        return Genre.values()[id - 1];
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return Map.of(
                "id", this.ordinal() + 1,
                "name", this.russianName
        );
    }
}