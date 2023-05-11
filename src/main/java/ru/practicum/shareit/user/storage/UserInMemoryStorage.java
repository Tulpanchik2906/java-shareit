package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserInMemoryStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new TreeSet<>();
    private Long id = 0l;

    @Override
    public User add(User user) {
        user.setId(generateNextId());
        users.put(user.getId(), user);
        emails.add(user.getEmail());
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
        return emails.contains(email);
    }

    @Override
    public User update(User user) {

        User userForUpdate = users.get(user.getId());
        emails.remove(userForUpdate.getEmail());

        users.put(user.getId(), user);
        emails.add(user.getEmail());

        return get(user.getId());
    }

    @Override
    public boolean delete(Long id) {
        User user = users.remove(id);
        if (user == null) {
            return false;
        } else {
            emails.remove(user.getEmail());
            return true;
        }
    }

    private Long generateNextId() {
        id++;
        return id;
    }

}
