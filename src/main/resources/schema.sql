-- Удаление таблиц в обратном порядке зависимостей
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS friends CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa_ratings CASCADE;

-- 1. Справочник жанров (без зависимостей)
CREATE TABLE IF NOT EXISTS genres (
                                      genre_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                      name VARCHAR(50) NOT NULL UNIQUE
    );

-- 2. Справочник MPA рейтингов (без зависимостей)
CREATE TABLE IF NOT EXISTS mpa_ratings (
                                           rating_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                           name VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(255)
    );

-- 3. Пользователи (без зависимостей)
CREATE TABLE IF NOT EXISTS users (
                                     user_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     email VARCHAR(100) NOT NULL UNIQUE,
    login VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    birthday DATE NOT NULL
    );

-- 4. Фильмы (зависит от mpa_ratings)
CREATE TABLE IF NOT EXISTS films (
                                     film_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL CHECK (duration > 0),
    rating_id INTEGER,
    FOREIGN KEY (rating_id) REFERENCES mpa_ratings(rating_id) ON DELETE SET NULL
    );

-- 5. Связь фильмов и жанров (зависит от films и genres)
CREATE TABLE IF NOT EXISTS film_genres (
                                           film_id INTEGER NOT NULL,
                                           genre_id INTEGER NOT NULL,
                                           PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE
    );

-- 6. Лайки (зависит от films и users)
CREATE TABLE IF NOT EXISTS likes (
                                     film_id INTEGER NOT NULL,
                                     user_id INTEGER NOT NULL,
                                     PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

-- 7. Дружба (зависит от users)
CREATE TABLE IF NOT EXISTS friends (
                                       user_id INTEGER NOT NULL,
                                       friend_id INTEGER NOT NULL,
                                       status VARCHAR(20) NOT NULL DEFAULT 'UNCONFIRMED',
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CHECK (user_id <> friend_id)
    );

-- Индексы для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_film_genres_film ON film_genres(film_id);
CREATE INDEX IF NOT EXISTS idx_film_genres_genre ON film_genres(genre_id);
CREATE INDEX IF NOT EXISTS idx_likes_film ON likes(film_id);
CREATE INDEX IF NOT EXISTS idx_likes_user ON likes(user_id);
CREATE INDEX IF NOT EXISTS idx_friends_user ON friends(user_id);
CREATE INDEX IF NOT EXISTS idx_friends_friend ON friends(friend_id);

-- Заполнение справочника жанров
MERGE INTO genres (genre_id, name) KEY(genre_id) VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

-- Заполнение справочника MPA рейтингов
MERGE INTO mpa_ratings (rating_id, name, description) KEY(rating_id) VALUES
    (1, 'G', 'У фильма нет возрастных ограничений'),
    (2, 'PG', 'Детям рекомендуется смотреть фильм с родителями'),
    (3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
    (4, 'R', 'Лицам до 17 лет просматривать фильм можно только в присутствии взрослого'),
    (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');