package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                         @Valid @RequestBody CreateItemRequestDto createItemRequestDto) {
        log.info("Получен запрос на вещь от пользователя {} ", userId);

        return itemRequestClient.create(userId, createItemRequestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id,
                                      @RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос на получения информации о запросе на вещи {} от пользователя {} ",
                id, userId);
        return itemRequestClient.get(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос на получения списка запросов на вещи от пользователя {} ", userId);

        return itemRequestClient.findAll(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllWithFromAndSize(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                                         @RequestParam(required = false) @PositiveOrZero Integer from,
                                                         @RequestParam(required = false) @Positive Integer size) {
        log.info("Получен запрос на получения списка запросов на вещи от пользователя {} ", userId);

        return itemRequestClient.findAllWithFromAndSize(userId, from, size);
    }
}
