package ru.practicum.shareit.user.client;

import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.PatchUserDto;

public interface UserClient {
    public ResponseEntity<Object> addUser(CreateUserDto user);

    public ResponseEntity<Object> updateUserById(Long id, PatchUserDto user);

    public ResponseEntity<Object> getUserById(Long id);

    public ResponseEntity<Object> deleteUserById(Long id);

    public ResponseEntity<Object> findAll();
}
