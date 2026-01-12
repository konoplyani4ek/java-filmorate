package ru.yandex.practicum.filmorate.repository.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User> {

    public UserRepository(JdbcTemplate jdbc) {
        super(jdbc, new UserRowMapper());
    }

    public User create(User user) {
        long id = insert(
                "insert into users(email, login, name, birthday) values (?, ?, ?, ?)",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId((int) id);
        return user;
    }

    public User update(User user) {
        update(
                "update users set email = ?, login = ?, name = ?, birthday = ? where user_id = ?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    public boolean deleteById(int id) {
        return delete("delete from users where user_id = ?", id);
    }

    public Optional<User> findById(int id) {
        return findOne("select * from users where user_id = ?", id);
    }

    public List<User> findAll() {
        return findMany("select * from users");
    }
}
