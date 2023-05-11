package ru.practicum.shareit.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public User addUser(@Valid @RequestBody UserDto user) {
        return userService.create(UserMapper.toUser(user));
    }

    @PatchMapping("/{id}")
    public User updateUserById(@PathVariable Long id, @Valid @RequestBody UserDto user) {
        return userService.update(id, UserMapper.toUser(user));
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.get(id);
    }

    @DeleteMapping("/{id}")
    public boolean deleteUserById(@PathVariable Long id) {
        return userService.delete(id);
    }

    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }
}
