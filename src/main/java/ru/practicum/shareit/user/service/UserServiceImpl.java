package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userStorage;

    @Override
    public User get(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id: " + id + " не найден."));
    }

    @Override
    public List<User> findAll() {
        return userStorage.findAll();
    }

    @Override
    @Transactional
    public User create(User user) {
        user.setEmail(user.getEmail());
        return userStorage.save(user);
    }

    @Override
    @Transactional
    public User update(Long userId, User userPatch) {
        User newUser = get(userId);

        if (userPatch.getName() != null) {
            newUser.setName(userPatch.getName());
        }

        if (userPatch.getEmail() != null) {
            newUser.setEmail(userPatch.getEmail());
        }

        return userStorage.save(newUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = get(id);
        userStorage.deleteById(id);
    }

}
