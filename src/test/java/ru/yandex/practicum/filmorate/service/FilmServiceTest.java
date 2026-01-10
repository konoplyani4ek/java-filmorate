package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.model.Film.Genre;
import ru.yandex.practicum.filmorate.model.Film.MpaRating;
import ru.yandex.practicum.filmorate.model.User.User;
import ru.yandex.practicum.filmorate.repository.repository.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Простые тесты для FilmService/FilmController.
 */
class FilmServiceTest {

    private FilmService filmService;
    private FilmController filmController;

    // Репозитории (моки)
    private FilmRepository filmRepository;
    private GenreRepository genreRepository;
    private LikeRepository likeRepository;
    private UserRepository userRepository;
    private GenreService genreService;
    private MpaRatingRepository mpaRepository;


    @BeforeEach
    void setUp() {
        // Создаем моки
        filmRepository = mock(FilmRepository.class);
        genreRepository = mock(GenreRepository.class);
        likeRepository = mock(LikeRepository.class);
        userRepository = mock(UserRepository.class);
        mpaRepository = mock(MpaRatingRepository.class);
        genreService = mock(GenreService.class);

        // Создаем сервис с новой сигнатурой (5 параметров)
        filmService = new FilmService(
                filmRepository,
                genreRepository,
                likeRepository,
                userRepository,
                genreService, mpaRepository
        );

        // Создаем контроллер
        filmController = new FilmController(filmService);
    }

    @Test
    void create_shouldCreateFilm() {
        // Arrange
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Film createdFilm = new Film();
        createdFilm.setId(1);
        createdFilm.setName("Test Film");
        createdFilm.setDescription("Description");
        createdFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        createdFilm.setDuration(120);
        createdFilm.setMpa(mpa);
        createdFilm.setGenres(new LinkedHashSet<>());

        // Моки
        when(mpaRepository.existsById(1)).thenReturn(true);
        when(mpaRepository.findById(1)).thenReturn(mpa);
        when(filmRepository.create(any(Film.class))).thenReturn(createdFilm);
        when(filmRepository.findById(1)).thenReturn(Optional.of(createdFilm));
        when(genreRepository.getGenresByFilmId(1)).thenReturn(new LinkedHashSet<>());

        // Act
        Film result = filmService.create(film);

        // Assert
        assertNotNull(result.getId());
        assertEquals(1, result.getId());
        verify(filmRepository, times(1)).create(any(Film.class));
    }

    @Test
    void getFilmById_shouldReturnFilm() {
        // Arrange
        Film film = new Film();
        film.setId(1);
        film.setName("Test Film");

        when(filmRepository.findById(1)).thenReturn(Optional.of(film));

        // Act
        Film result = filmService.getFilmById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Film", result.getName());
        verify(filmRepository, times(1)).findById(1);
    }

    @Test
    void getAllFilms_shouldReturnAllFilms() {
        // Arrange
        Film film1 = new Film();
        film1.setId(1);
        film1.setName("Film 1");

        Film film2 = new Film();
        film2.setId(2);
        film2.setName("Film 2");

        when(filmRepository.findAll()).thenReturn(Arrays.asList(film1, film2));

        // Act
        Collection<Film> films = filmService.getAllFilms();

        // Assert
        assertEquals(2, films.size());
        verify(filmRepository, times(1)).findAll();
    }

    @Test
    void addLike_shouldAddLike() {
        // Arrange
        Film film = new Film();
        film.setId(1);

        User user = new User();
        user.setId(2);

        when(filmRepository.findById(1)).thenReturn(Optional.of(film));
        when(userRepository.findById(2)).thenReturn(Optional.of(user));
        when(likeRepository.hasLike(1, 2)).thenReturn(false);

        // Act
        filmService.addLike(1, 2);

        // Assert
        verify(likeRepository, times(1)).addLike(1, 2);
    }

    @Test
    void addLike_shouldNotThrowWhenAlreadyLiked() {
        // Arrange
        Film film = new Film();
        film.setId(1);

        User user = new User();
        user.setId(2);

        when(filmRepository.findById(1)).thenReturn(Optional.of(film));
        when(userRepository.findById(2)).thenReturn(Optional.of(user));
        when(likeRepository.hasLike(1, 2)).thenReturn(true); // Уже есть лайк

        // Act - не должно выбросить исключение
        filmService.addLike(1, 2);

        // Assert - лайк НЕ добавляется повторно
        verify(likeRepository, never()).addLike(anyInt(), anyInt());
    }

    @Test
    void removeLike_shouldRemoveLike() {
        // Arrange
        Film film = new Film();
        film.setId(1);

        User user = new User();
        user.setId(2);

        when(filmRepository.findById(1)).thenReturn(Optional.of(film));
        when(userRepository.findById(2)).thenReturn(Optional.of(user));

        // Act
        filmService.removeLike(1, 2);

        // Assert
        verify(likeRepository, times(1)).removeLike(1, 2);
    }

    @Test
    void getTopMostLikedFilms_shouldReturnTopFilms() {
        // Arrange
        Film film1 = new Film();
        film1.setId(1);
        film1.setName("Popular Film");

        Film film2 = new Film();
        film2.setId(2);
        film2.setName("Less Popular Film");

        when(filmRepository.findAll()).thenReturn(Arrays.asList(film1, film2));
        when(likeRepository.countLikes(1)).thenReturn(10);
        when(likeRepository.countLikes(2)).thenReturn(5);

        // Act
        Collection<Film> topFilms = filmService.getTopMostLikedFilms(1);

        // Assert
        assertEquals(1, topFilms.size());
        assertEquals("Popular Film", topFilms.iterator().next().getName());
    }

    @Test
    void update_shouldUpdateFilm() {
        // Arrange
        Film existingFilm = new Film();
        existingFilm.setId(1);
        existingFilm.setName("Old Name");

        Film updatedFilm = new Film();
        updatedFilm.setId(1);
        updatedFilm.setName("New Name");
        updatedFilm.setDescription("New Description");
        updatedFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        updatedFilm.setDuration(150);

        when(filmRepository.findById(1)).thenReturn(Optional.of(existingFilm));
        when(filmRepository.update(any(Film.class))).thenReturn(updatedFilm);

        // Act
        Film result = filmService.update(updatedFilm);

        // Assert
        assertEquals("New Name", result.getName());
        verify(filmRepository, times(1)).update(any(Film.class));
    }

    @Test
    void deleteFilm_shouldDeleteFilm() {
        // Arrange
        Film film = new Film();
        film.setId(1);

        when(filmRepository.findById(1)).thenReturn(Optional.of(film));
        when(filmRepository.deleteById(1)).thenReturn(true);

        // Act
        filmService.deleteFilm(1);

        // Assert
        verify(filmRepository, times(1)).deleteById(1);
    }

    @Test
    void create_withGenres_shouldValidateGenres() {
        // Arrange
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(new LinkedHashSet<>(Arrays.asList(genre)));

        Film createdFilm = new Film();
        createdFilm.setId(1);
        createdFilm.setName("Test Film");
        createdFilm.setMpa(mpa);
        createdFilm.setGenres(new LinkedHashSet<>(Arrays.asList(genre)));

        // Моки
        when(mpaRepository.existsById(1)).thenReturn(true);
        when(mpaRepository.findById(1)).thenReturn(mpa);
        when(genreService.getGenreById(1)).thenReturn(genre);
        when(filmRepository.create(any(Film.class))).thenReturn(createdFilm);
        when(filmRepository.findById(1)).thenReturn(Optional.of(createdFilm));
        when(genreRepository.getGenresByFilmId(1)).thenReturn(new LinkedHashSet<>(Arrays.asList(genre)));
        doNothing().when(genreService).validateGenres(any());

        // Act
        Film result = filmService.create(film);

        // Assert
        assertNotNull(result.getId());
        verify(genreService, times(1)).validateGenres(any());
        verify(genreService, times(1)).getGenreById(1); // Проверка заполнения жанров
    }
}