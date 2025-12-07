package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {

    int id;
    String name;
    String description;
    LocalDate releaseDate;
    @JsonIgnore
    Duration duration;

    @JsonProperty("duration")
    public long getDurationMinutes() {
        return duration.toMinutes();
    }

    public void setDurationMinutes(long minutes) {
        this.duration = Duration.ofMinutes(minutes);
    }
}
