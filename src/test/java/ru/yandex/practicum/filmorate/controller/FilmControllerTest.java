package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.repository.repository.*;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты для FilmController.
 * Используют простые моки вместо Spring Context.
 */
public class FilmControllerTest {

    private FilmController filmController;
    private FilmService filmService;

    // Репозитории (моки)
    private FilmRepository filmRepository;
    private GenreRepository genreRepository;
    private LikeRepository likeRepository;
    private UserRepository userRepository;
    private GenreService genreService;

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    @BeforeEach
    void setUp() {
        // Создаем моки
        filmRepository = mock(FilmRepository.class);
        genreRepository = mock(GenreRepository.class);
        likeRepository = mock(LikeRepository.class);
        userRepository = mock(UserRepository.class);
        genreService = mock(GenreService.class);

        // Создаем сервис с новой сигнатурой (5 параметров)
        filmService = new FilmService(
                filmRepository,
                genreRepository,
                likeRepository,
                userRepository,
                genreService
        );

        // Создаем контроллер
        filmController = new FilmController(filmService);
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    private Film filmWithEmptyName() {
        Film film = validFilm();
        film.setName("");
        return film;
    }

    private Film filmWithNegativeDuration() {
        Film film = validFilm();
        film.setDuration(-10);
        return film;
    }

    private Film filmWithTooEarlyReleaseDate() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        return film;
    }

    @Test
    void createFilm_ShouldReturnCreatedFilm() {
        // Arrange
        Film film = validFilm();

        Film createdFilm = new Film();
        createdFilm.setId(1);
        createdFilm.setName("Test Film");
        createdFilm.setDescription("Описание фильма");
        createdFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        createdFilm.setDuration(120);

        when(filmRepository.create(any(Film.class))).thenReturn(createdFilm);
        when(filmRepository.findAll()).thenReturn(Arrays.asList(createdFilm));

        // Act
        Film created = filmController.create(film);
        Collection<Film> allFilms = filmController.getAllFilms();

        // Assert
        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
        assertTrue(allFilms.contains(created));

        verify(filmRepository, times(1)).create(any(Film.class));
    }

    @Test
    void createFilm_ShouldFail_WhenNameEmpty() {
        // Arrange
        Film film = filmWithEmptyName();

        // Act
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Assert
        assertFalse(violations.isEmpty());

        boolean hasNameError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertTrue(hasNameError, "Фильм с пустым именем должен вызвать ошибку валидации");
    }

    @Test
    void createFilm_ShouldFail_WhenDurationNotPositive() {
        // Arrange
        Film film = filmWithNegativeDuration();

        // Act
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Assert
        assertFalse(violations.isEmpty());

        boolean hasDurationError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration") &&
                        v.getMessage().contains("положительным числом"));
        assertTrue(hasDurationError, "Фильм с нулевой длительностью должен вызвать ошибку валидации");
    }

    @Test
    void shouldFailValidation_whenReleaseDateTooEarly() {
        // Arrange
        Film film = filmWithTooEarlyReleaseDate();

        // Act
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getMessage()
                                .contains("дата релиза — не раньше 28 декабря 1895 года"))
        );
    }

    @Test
    void updateFilm_ShouldUpdateExistingFilm() {
        // Arrange
        Film film = validFilm();

        Film createdFilm = new Film();
        createdFilm.setId(1);
        createdFilm.setName("Test Film");
        createdFilm.setDescription("Описание фильма");
        createdFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        createdFilm.setDuration(120);

        Film updatedFilm = new Film();
        updatedFilm.setId(1);
        updatedFilm.setName("New Name");
        updatedFilm.setDescription("Описание фильма");
        updatedFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        updatedFilm.setDuration(150);

        when(filmRepository.create(any(Film.class))).thenReturn(createdFilm);
        when(filmRepository.findById(1)).thenReturn(Optional.of(createdFilm));
        when(filmRepository.update(any(Film.class))).thenReturn(updatedFilm);

        // Act
        Film created = filmController.create(film);

        Film filmToUpdate = validFilm();
        filmToUpdate.setId(created.getId());
        filmToUpdate.setName("New Name");
        filmToUpdate.setDuration(150);

        Film updated = filmController.update(filmToUpdate);

        // Assert
        assertEquals("New Name", updated.getName());
        assertEquals(150, updated.getDuration());

        verify(filmRepository, times(1)).update(any(Film.class));
    }

    @Test
    void updateFilm_ShouldThrowEntityNotFoundException_WhenFilmNotFound() {
        // Arrange
        Film film = validFilm();
        film.setId(999); // несуществующий ID

        when(filmRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> filmController.update(film));

        assertTrue(exception.getMessage().contains("не найден"));

        verify(filmRepository, times(1)).findById(999);
        verify(filmRepository, never()).update(any(Film.class));
    }

    @Test
    void getAll_ShouldReturnAllFilms() {
        // Arrange
        Film film1 = new Film();
        film1.setId(1);
        film1.setName("Test Film 1");
        film1.setDescription("Описание 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setId(2);
        film2.setName("Test Film 2");
        film2.setDescription("Описание 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(90);

        when(filmRepository.create(any(Film.class)))
                .thenReturn(film1)
                .thenReturn(film2);
        when(filmRepository.findAll()).thenReturn(Arrays.asList(film1, film2));

        // Act
        Film created1 = filmController.create(validFilm());
        Film created2 = filmController.create(validFilm());
        Collection<Film> allFilms = filmController.getAllFilms();

        // Assert
        assertEquals(2, allFilms.size());
        assertTrue(allFilms.contains(film1));
        assertTrue(allFilms.contains(film2));

        verify(filmRepository, times(1)).findAll();
    }
}