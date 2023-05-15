package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class UserInMemoryStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    private Long id = 0L;

    @Override
    public User add(User user) {
        user.setId(generateNextId());
        users.put(user.getId(), user);

        log.info("Добавлен новый пользователь с id : {}", user.getId());

        return get(user.getId());
    }

    @Override
    public User get(Long id) {
        return users.get(id);
    }

    @Override
    public List<User> findAll() {
        return users.values().stream().collect(Collectors.toList());
    }

    @Override
    public boolean containsEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().compareTo(email) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public User update(User user) {

        User userForUpdate = users.get(user.getId());

        users.put(user.getId(), user);

        log.info("Обновлен пользователь с id : {}", user.getId());

        return get(user.getId());
    }

    @Override
    public boolean delete(Long id) {
        User user = users.remove(id);
        if (user == null) {
            log.info("Не удалось удалить пользователя с id : {}", user.getId());
            return false;
        } else {
            log.info("Удален пользователь с id : {}", user.getId());
            return true;
        }
    }

    private Long generateNextId() {
        id++;
        return id;
    }

}
