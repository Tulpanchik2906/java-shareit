package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    public User add(User user);

    public User get(Long id);

    public List<User> findAll();

    public boolean containsEmail(String email);

    public User update(User user);

    public boolean delete(Long id);

}
