package ru.practicum.shareit.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.exception.NotFoundException;

public interface UserRepository extends JpaRepository<User, Long> {
    default User getExistUser(Long userId) {
        return findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id: " + userId + " не найден."));
    }
}
