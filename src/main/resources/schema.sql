

DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS friends CASCADE;
DROP TABLE IF EXISTS friendship_status CASCADE;
DROP TABLE IF EXISTS genres CASCADE;


CREATE TABLE genres (
                        genre_id INTEGER AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL UNIQUE
);


CREATE TABLE film_genres (
                             film_id INTEGER NOT NULL,
                             genre_id INTEGER NOT NULL,
                             PRIMARY KEY (film_id, genre_id),
                             FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
                             FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE
);

-- Индексы для film_genres
CREATE INDEX idx_film_genres_film_id ON film_genres(film_id);
CREATE INDEX idx_film_genres_genre_id ON film_genres(genre_id);


CREATE TABLE friends (
                         user_id INTEGER NOT NULL,
                         friend_id INTEGER NOT NULL,
                         status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (user_id, friend_id),
                         FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                         FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Индексы для friends
CREATE INDEX idx_friends_user_id ON friends(user_id);
CREATE INDEX idx_friends_friend_id ON friends(friend_id);
CREATE INDEX idx_friends_status ON friends(status);

-- Шаг 5: Заполняем жанры (MERGE предотвращает дубликаты)
MERGE INTO genres (genre_id, name) KEY(genre_id) VALUES (1, 'Комедия');
MERGE INTO genres (genre_id, name) KEY(genre_id) VALUES (2, 'Драма');
MERGE INTO genres (genre_id, name) KEY(genre_id) VALUES (3, 'Мультфильм');
MERGE INTO genres (genre_id, name) KEY(genre_id) VALUES (4, 'Триллер');
MERGE INTO genres (genre_id, name) KEY(genre_id) VALUES (5, 'Документальный');
MERGE INTO genres (genre_id, name) KEY(genre_id) VALUES (6, 'Боевик');