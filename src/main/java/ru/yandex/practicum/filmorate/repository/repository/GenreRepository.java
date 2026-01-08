package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film.Genre;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class GenreRepository {
    private final JdbcTemplate jdbc;

    public GenreRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Возвращает id жанра; если записи в справочнике нет — создаёт её.
     */
    public int getOrCreateId(Genre genre) {
        if (genre == null) {
            throw new IllegalArgumentException("genre не должен быть null");
        }

        Optional<Integer> existing = findIdByName(genre.name());
        if (existing.isPresent()) {
            return existing.get();
        }

        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into genres(name) values (?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, genre.name());
            return ps;
        }, kh);

        Integer id = kh.getKeyAs(Integer.class);
        if (id == null) {
            throw new InternalServerException("Не удалось сохранить genre");
        }
        return id;
    }

    public Optional<Integer> findIdByName(String name) {
        try {
            Integer id = jdbc.queryForObject(
                    "select genre_id from genres where name = ?",
                    Integer.class,
                    name
            );
            return Optional.ofNullable(id);
        } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Set<Genre> getGenresByFilmId(int filmId) {
        List<String> names = jdbc.queryForList(
                "select g.name " +
                        "from film_genres fg " +
                        "join genres g on g.genre_id = fg.genre_id " +
                        "where fg.film_id = ?",
                String.class,
                filmId
        );
        Set<Genre> genres = new LinkedHashSet<>();
        for (String n : names) {
            if (n != null) {
                try {
                    genres.add(Genre.valueOf(n.trim()));
                } catch (IllegalArgumentException e) {
                    // If genre not found, skip it (for compatibility with old data)
                    continue;
                }
            }
        }
        return genres;
    }

    /**
     * Возвращает жанры для набора filmId одним запросом.
     */
    public Map<Integer, Set<Genre>> getGenresByFilmIds(Set<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "select fg.film_id, g.name " +
                "from film_genres fg " +
                "join genres g on g.genre_id = fg.genre_id " +
                "where fg.film_id in (" + placeholders + ")";

        Object[] args = filmIds.toArray();

        Map<Integer, Set<Genre>> result = new HashMap<>();
        jdbc.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            String name = rs.getString("name");
            if (name == null) return;

            try {
                Genre genre = Genre.valueOf(name.trim());
                result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            } catch (IllegalArgumentException e) {
                // If genre not found, skip it (for compatibility with old data)
            }
        }, args);

        return result;
    }

    /**
     * Перезаписывает жанры фильма: сначала чистим, потом вставляем текущие.
     */
    public void setGenresForFilm(int filmId, Set<Genre> genres) {
        jdbc.update("delete from film_genres where film_id = ?", filmId);

        if (genres == null || genres.isEmpty()) {
            return;
        }

        for (Genre g : genres) {
            int genreId = getOrCreateId(g);
            jdbc.update(
                    "insert into film_genres(film_id, genre_id) values (?, ?)",
                    filmId,
                    genreId
            );
        }
    }
}