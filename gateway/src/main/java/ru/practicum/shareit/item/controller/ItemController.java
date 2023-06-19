package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClientImp;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.PatchItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@AllArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClientImp itemClientImp;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public ResponseEntity<Object> findAllByUserId(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                                  @RequestParam(required = false) @PositiveOrZero Integer from,
                                                  @RequestParam(required = false) @Positive Integer size) {
        log.info("Получен запрос на получение всего списка вещей пользователя {}.",
                userId);

        return itemClientImp.findAllByUserId(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id,
                                      @RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос от пользвателя {} на получение информации о вещи с id: {}.",
                userId, id);

        return itemClientImp.getItem(id, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                         @RequestParam(required = false) String text,
                                         @RequestParam(required = false) @PositiveOrZero Integer from,
                                         @RequestParam(required = false) @Positive Integer size) {
        log.info("Получен запрос на получение списка вещей пользователя {}" +
                " по поиску: {}.", userId, text);

        return itemClientImp.search(userId, text, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                      @Valid @RequestBody CreateItemDto item) {
        log.info("Получен запрос на сохранение новой вещи пользователя {} ", userId);

        return itemClientImp.add(userId, item);
    }


    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long id,
                                             @RequestHeader(X_SHARER_USER_ID) Long userId,
                                             @Valid @RequestBody CreateCommentDto comment) {
        log.info("Получен запрос на добавления комментария " +
                "для вещи с id {} от пользователя {}", id, userId);
        return itemClientImp.addComment(id, userId, comment);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id,
                                         @RequestHeader(X_SHARER_USER_ID) Long userId,
                                         @RequestBody PatchItemDto item) {
        log.info("Получен запрос на обновлении вещи пользователя {} ", userId);

        return itemClientImp.update(id, userId, item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long id,
                                             @RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос на удалении вещи {} пользователя {} ", id, userId);

        return itemClientImp.deleteItem(id, userId);
    }

}
