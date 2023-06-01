package ru.practicum.shareit.user.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class UserUtil {

    private final UserRepository userRepository;

    public User getExistUser(Long userId) {
        if (userId != null) {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return user.get();
            }
        }
        log.error("Пользователь с id: {} не найден.", userId);
        throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
    }
}
