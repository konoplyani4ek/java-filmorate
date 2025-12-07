package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        return film;
    }

    private Film filmWithEmptyName() {
        Film film = validFilm();
        film.setName("");
        return film;
    }

    private Film filmWithNegativeDuration() {
        Film film = validFilm();
        film.setDuration(Duration.ofMinutes(-10));
        return film;
    }

    private Film filmWithTooEarlyReleaseDate() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        return film;
    }


    @Test
    void createFilm_ShouldReturnCreatedFilm() {
        Film created = filmController.create(validFilm());

        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());

        Collection<Film> allFilms = filmController.getAll();
        assertTrue(allFilms.contains(created));
    }

    @Test
    void createFilm_ShouldThrowValidateException_WhenNameEmpty() {
        ValidateException exception = assertThrows(ValidateException.class,
                () -> filmController.create(filmWithEmptyName()));

        assertTrue(exception.getMessage().contains("название не может быть пустым"));
    }

    @Test
    void createFilm_ShouldThrowValidateException_WhenDurationNegative() {
        ValidateException exception = assertThrows(ValidateException.class,
                () -> filmController.create(filmWithNegativeDuration()));

        assertTrue(exception.getMessage().contains("продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void createFilm_ShouldThrowValidateException_WhenReleaseDateTooEarly() {
        ValidateException exception = assertThrows(ValidateException.class,
                () -> filmController.create(filmWithTooEarlyReleaseDate()));

        assertTrue(exception.getMessage().contains("дата релиза — не раньше 28 декабря 1895 года"));
    }


    @Test
    void updateFilm_ShouldUpdateExistingFilm() {
        Film created = filmController.create(validFilm());

        Film updatedFilm = validFilm();
        updatedFilm.setId(created.getId());
        updatedFilm.setName("New Name");
        updatedFilm.setDuration(Duration.ofMinutes(150));

        Film updated = filmController.update(updatedFilm);

        assertEquals("New Name", updated.getName());
        assertEquals(150, updated.getDuration().toMinutes());
    }

    @Test
    void updateFilm_ShouldThrowValidateException_WhenFilmNotFound() {
        Film film = validFilm();
        film.setId(999); // несуществующий ID

        ValidateException exception = assertThrows(ValidateException.class,
                () -> filmController.update(film));

        assertTrue(exception.getMessage().contains("не найден"));
    }

    @Test
    void getAll_ShouldReturnAllFilms() {
        Film film1 = filmController.create(validFilm());
        Film film2 = filmController.create(validFilm());

        Collection<Film> allFilms = filmController.getAll();

        assertEquals(2, allFilms.size());
        assertTrue(allFilms.contains(film1));
        assertTrue(allFilms.contains(film2));
    }
}
