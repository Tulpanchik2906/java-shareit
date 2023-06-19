package ru.practicum.shareit.request.client;

import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

public interface ItemRequestClient {
    public ResponseEntity<Object> create(
            Long userId, CreateItemRequestDto createItemRequestDto);

    public ResponseEntity<Object> get(Long id, Long userId);

    public ResponseEntity<Object> findAll(Long userId);

    public ResponseEntity<Object> findAllWithFromAndSize(
            Long userId, Integer from, Integer size);

}
