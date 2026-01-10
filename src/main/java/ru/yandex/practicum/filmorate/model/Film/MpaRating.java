package ru.yandex.practicum.filmorate.model.Film;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpaRating {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    private String description;

    // Конструктор для создания только с id (для десериализации)
    public MpaRating(Integer id) {
        this.id = id;
    }
}