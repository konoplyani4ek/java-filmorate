package ru.yandex.practicum.filmorate.repository.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Genre> GENRE_ROW_MAPPER = new RowMapper<>() {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Genre(
                    rs.getInt("genre_id"),
                    rs.getString("name")
            );
        }
    };

    /**
     * Получить все жанры из БД.
     */
    public List<Genre> findAll() {
        String sql = "SELECT genre_id, name FROM genres ORDER BY genre_id";
        log.debug("Executing query: {}", sql);
        return jdbc.query(sql, GENRE_ROW_MAPPER);
    }

    /**
     * Получить жанр по ID.
     */
    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
        log.debug("Executing query: {} with id={}", sql, id);
        try {
            Genre genre = jdbc.queryForObject(sql, GENRE_ROW_MAPPER, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Genre with id={} not found", id);
            return Optional.empty();
        }
    }

    /**
     * Получить жанры для конкретного фильма.
     */
    public Set<Genre> getGenresByFilmId(Integer filmId) {
        String sql = "SELECT g.genre_id, g.name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";

        log.debug("Getting genres for film_id={}", filmId);
        List<Genre> genres = jdbc.query(sql, GENRE_ROW_MAPPER, filmId);
        return new LinkedHashSet<>(genres);
    }

    /**
     * Получить жанры для набора фильмов одним запросом.
     */
    public Map<Integer, Set<Genre>> getGenresByFilmIds(Set<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id IN (" + placeholders + ") " +
                "ORDER BY fg.film_id, g.genre_id";

        Object[] args = filmIds.toArray();
        log.debug("Getting genres for {} films", filmIds.size());

        Map<Integer, Set<Genre>> result = new HashMap<>();
        jdbc.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre(
                    rs.getInt("genre_id"),
                    rs.getString("name")
            );
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }, args);

        return result;
    }

    /**
     * Установить жанры для фильма.
     * Удаляет старые связи и создает новые.
     */
    public void setGenresForFilm(Integer filmId, Set<Genre> genres) {
        log.debug("Setting genres for film_id={}", filmId);

        // Удаляем старые связи
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteSql, filmId);

        if (genres == null || genres.isEmpty()) {
            log.debug("No genres to add for film_id={}", filmId);
            return;
        }

        // Добавляем новые связи
        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            if (genre.getId() != null) {
                jdbc.update(insertSql, filmId, genre.getId());
                log.debug("Added genre_id={} to film_id={}", genre.getId(), filmId);
            }
        }
    }

    /**
     * Удалить все жанры фильма.
     */
    public void deleteGenresForFilm(Integer filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(sql, filmId);
        log.debug("Deleted all genres for film_id={}", filmId);
    }

    /**
     * Проверить существование жанра.
     */
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}