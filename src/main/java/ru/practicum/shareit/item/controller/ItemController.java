package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@AllArgsConstructor
@Slf4j
public class ItemController {
    private final ItemServiceImpl itemService;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDto> findAllByUserId(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос на получение всего списка вещей пользователя {}.",
                userId);

        return itemService.findAllByUser(userId).stream()
                .map(item -> ItemMapper.toItemDto(item))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ItemDto get(@PathVariable Long id,
                       @RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос от пользвателя {} на получение информации о вещи с id: {}.",
                userId, id);

        return ItemMapper.toItemDto(itemService.getInfo(id, userId));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                @RequestParam(required = false) String text) {
        log.info("Получен запрос на получение списка вещей пользователя {}" +
                " по поиску: {}.", userId, text);

        return itemService.searchItems(userId, text).stream()
                .map(item -> ItemMapper.toItemDto(item))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ItemDto add(@RequestHeader(X_SHARER_USER_ID) Long userId,
                       @Valid @RequestBody CreateItemDto item) {
        log.info("Получен запрос на сохранение новой вещи пользователя {} ", userId);

        return ItemMapper.toItemDto(
                itemService.create(userId, item.getRequestId(),
                        ItemMapper.toItem(item)));
    }


    @PostMapping("/{id}/comment")
    public CommentDto addComment(@PathVariable Long id,
                                 @RequestHeader(X_SHARER_USER_ID) Long userId,
                                 @Valid @RequestBody CreateCommentDto comment) {
        log.info("Получен запрос на добавления комментария " +
                "для вещи с id {} от пользователя {}", id, userId);
        return CommentMapper.toCommentDto(
                itemService.addComment(id, userId, CommentMapper.toComment(comment)));
    }


    @PatchMapping("/{id}")

    public ItemDto update(@PathVariable Long id,
                          @RequestHeader(X_SHARER_USER_ID) Long userId,
                          @RequestBody PatchItemDto item) {
        log.info("Получен запрос на обновлении вещи пользователя {} ", userId);

        return ItemMapper.toItemDto(
                itemService.update(id, userId, item.getRequestId(),
                        ItemMapper.toItem(item)));
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id,
                           @RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Получен запрос на удалении вещи {} пользователя {} ", id, userId);

        itemService.delete(userId, id);
    }

}
