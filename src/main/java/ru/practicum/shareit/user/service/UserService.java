package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.util.exception.ValidationException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;

    public User get(Long id) {
        User user = userStorage.get(id);
        if (user == null) {
            throw new NullPointerException("Пользователь с id: " + id + " не найден.");
        }
        return user;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) throws ValidationException {
        if (!userStorage.containsEmail(user.getEmail())) {
            return userStorage.add(user);
        } else {
            throw new ValidationException("Пользователь с Email: " + user.getEmail() + " уже существует.");
        }
    }

    public User update(Long userId, User user) throws ValidationException {
        if (userStorage.get(userId) == null) {
            throw new NullPointerException("Пользователь с id: " + userId + " уже существует.");
        }

        user.setId(userId);

        if (!userStorage.containsEmail(user.getEmail())) {
            return userStorage.add(user);
        } else {
            throw new ValidationException("Пользователь с Email: " + user.getEmail() + " уже существует.");
        }
    }

    public boolean delete(Long id) {
        User user = userStorage.get(id);
        if (user == null) {
            throw new NullPointerException("Пользователь с id: " + id + " не найден.");
        }
        return userStorage.delete(id);
    }

}
