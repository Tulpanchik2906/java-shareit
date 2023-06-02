package ru.practicum.shareit.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByEmailContainingIgnoreCase(String emailSearch);

    default User getExistUser(Long userId) {
        if (userId != null) {
            Optional<User> user = findById(userId);
            if (user.isPresent()) {
                return user.get();
            }
        }
        throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
    }
}
