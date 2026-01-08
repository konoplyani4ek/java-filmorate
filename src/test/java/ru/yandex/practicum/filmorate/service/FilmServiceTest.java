package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film.Film;
import ru.yandex.practicum.filmorate.model.User.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage.InMemoryLikeStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage.UserStorage;


import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;
    private UserService userService;
    private LikeStorage likeStorage;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        likeStorage = new InMemoryLikeStorage();
        userService = new UserService(userStorage);
        filmService = new FilmService(filmStorage, likeStorage, userStorage);
    }

    @Test
    void addLike_shouldAddLikeToFilm() {
        User user = createUser("user@mail.ru");
        Film film = createFilm("Film 1");

        filmService.addLike(film.getId(), user.getId());

        assertTrue(likeStorage.hasLike(film.getId(), user.getId()));
        assertEquals(1, likeStorage.countLikes(film.getId()));
    }

    @Test
    void removeLike_shouldRemoveLikeFromFilm() {
        User user = createUser("user@mail.ru");
        Film film = createFilm("Film 1");

        filmService.addLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user.getId());

        assertFalse(likeStorage.hasLike(film.getId(), user.getId()));
        assertEquals(0, likeStorage.countLikes(film.getId()));
    }

    @Test
    void getTopMostLikedFilms_shouldReturnFilmsSortedByLikes() {
        User user1 = createUser("user1@mail.ru");
        User user2 = createUser("user2@mail.ru");

        Film film1 = createFilm("Film 1");
        Film film2 = createFilm("Film 2");

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());
        filmService.addLike(film2.getId(), user1.getId());

        Collection<Film> topFilms = filmService.getTopMostLikedFilms(1);

        assertEquals(1, topFilms.size());
        assertEquals(film1.getId(), topFilms.iterator().next().getId());
    }

    @Test
    void create_shouldCreateFilm() {
        Film film = createFilm("New Film");

        assertNotNull(film.getId());
        assertEquals(1, filmService.getAllFilms().size());
    }

    @Test
    void update_shouldUpdateFilm() {
        Film film = createFilm("Old Name");
        film.setName("Updated Name");

        Film updated = filmService.update(film);

        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void getAllFilms_shouldReturnAllFilms() {
        createFilm("Film 1");
        createFilm("Film 2");

        Collection<Film> films = filmService.getAllFilms();

        assertEquals(2, films.size());
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(email.split("@")[0]);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userService.create(user);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return filmService.create(film);
    }
}
