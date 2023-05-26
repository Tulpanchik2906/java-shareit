package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User get(Long id);

    List<User> findAll();

    User create(User user);

    User update(Long userId, User user);

    boolean delete(Long id);
}
