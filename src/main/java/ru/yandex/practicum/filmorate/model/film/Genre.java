package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Genre {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    public Genre() {
    }

    public Genre(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Конструктор для создания жанра только с id (для десериализации из JSON).
     * Используется когда клиент отправляет только {"id": 1}
     */
    @JsonCreator
    public Genre(@JsonProperty("id") Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return id != null && id.equals(genre.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Genre{id=" + id + ", name='" + name + "'}";
    }
}