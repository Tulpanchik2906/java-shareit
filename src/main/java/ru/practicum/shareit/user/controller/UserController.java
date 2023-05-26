package ru.practicum.shareit.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.PatchUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
@Slf4j
public class UserController {
    private final UserServiceImpl userService;

    @PostMapping
    public UserDto addUser(@Valid @RequestBody CreateUserDto user) {
        log.info("Получен запрос на создание нового пользователя.");
        return UserMapper.toUserDto(userService.create(UserMapper.toUser(user)));
    }

    @PatchMapping("/{id}")
    public UserDto updateUserById(@PathVariable Long id, @Valid @RequestBody PatchUserDto user) {
        log.info("Получен запрос на обновление данных пользователя с id: {} .", id);
        return UserMapper.toUserDto(userService.update(id, UserMapper.toUser(user)));
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Получен запрос на получение данных о пользователе с id: {}.", id);
        return UserMapper.toUserDto(userService.get(id));
    }

    @DeleteMapping("/{id}")
    public boolean deleteUserById(@PathVariable Long id) {
        log.info("Получен запрос на удаление пользователя с id: {} .", id);
        return userService.delete(id);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("Получен запрос на получение всего списка пользователей.");
        return userService.findAll().stream()
                .map(user -> UserMapper.toUserDto(user))
                .collect(Collectors.toList());
    }
}
