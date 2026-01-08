package ru.yandex.practicum.filmorate.model.Film;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Map;

@Getter
public enum MpaRating {
    G("G", "У фильма нет возрастных ограничений"),
    PG("PG", "Детям рекомендуется смотреть фильм с родителями"),
    PG_13("PG-13", "Детям до 13 лет просмотр не желателен"),
    R("R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17("NC-17", "Лицам до 18 лет просмотр запрещён");

    private final String displayName;
    private final String description;

    MpaRating(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return Map.of(
                "id", this.ordinal() + 1,
                "name", this.displayName
        );
    }
}