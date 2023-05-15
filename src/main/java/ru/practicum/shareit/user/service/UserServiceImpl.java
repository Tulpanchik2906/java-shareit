package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.util.exception.DuplicateEmailException;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public User get(Long id) {
        User user = userStorage.get(id);
        if (user == null) {
            log.error("Пользователь с id: {} не найден.", id);
            throw new NotFoundException("Пользователь с id: " + id + " не найден.");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return userStorage.findAll();
    }

    @Override
    public User create(User user) {
        validExistEmail(user.getEmail());
        return userStorage.add(user);
    }

    @Override
    public User update(Long userId, User userPatch) {
        User newUser = get(userId);

        if (userPatch.getName() != null) {
            newUser.setName(userPatch.getName());
        }

        if (userPatch.getEmail() != null) {
            if (userPatch.getEmail().compareTo(newUser.getEmail()) != 0) {
                validExistEmail(userPatch.getEmail());
            }
            newUser.setEmail(userPatch.getEmail());
        }

        return userStorage.update(newUser);
    }

    @Override
    public boolean delete(Long id) {
        User user = get(id);
        return userStorage.delete(id);
    }


    private void validExistEmail(String email) {
        if (userStorage.containsEmail(email)) {
            log.error("Пользователь с Email: {} уже существует.", email);
            throw new DuplicateEmailException("Пользователь с Email: " + email + " уже существует.");
        }
    }
}
