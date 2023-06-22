package ru.practicum.shareit.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClientImp;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.PatchUserDto;

import javax.validation.Valid;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
@Slf4j
public class UserController {
    private final UserClientImp userClientImp;

    @PostMapping
    public ResponseEntity<Object> addUser(@Valid @RequestBody CreateUserDto user) {
        log.info("Получен запрос на создание нового пользователя.");
        return userClientImp.addUser(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUserById(@PathVariable Long id,
                                                 @Valid @RequestBody PatchUserDto user) {
        log.info("Получен запрос на обновление данных пользователя с id: {} .", id);
        return userClientImp.updateUserById(id, user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        log.info("Получен запрос на получение данных о пользователе с id: {}.", id);
        return userClientImp.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable Long id) {
        log.info("Получен запрос на удаление пользователя с id: {} .", id);
        return userClientImp.deleteUserById(id);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Получен запрос на получение всего списка пользователей.");
        return userClientImp.findAll();
    }
}
