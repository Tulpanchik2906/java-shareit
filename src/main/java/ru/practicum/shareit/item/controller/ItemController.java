package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemDto;
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

    @GetMapping
    public List<ItemDto> findAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Получен запрос на получение всего списка вещей пользователя {}.",
                userId);

        return itemService.findAllByUser(userId).stream()
                .map(item -> ItemMapper.toItemDto(item))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ItemDto get(@PathVariable Long id,
                       @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Получен запрос от пользвателя {} на получение информации о вещи с id: {}.",
                userId, id);

        return ItemMapper.toItemDto(itemService.getInfo(id, userId));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @RequestParam(required = false) String text) {

        log.info("Получен запрос на получение списка вещей пользователя {}" +
                " по поиску: {}.", userId, text);

        return itemService.searchItems(userId, text).stream()
                .map(item -> ItemMapper.toItemDto(item))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @Valid @RequestBody CreateItemDto item) {

        log.info("Получен запрос на сохранение новой вещи пользователя {} ", userId);

        return ItemMapper.toItemDto(
                itemService.create(userId, ItemMapper.toItem(item)));
    }

    @PatchMapping("/{id}")
    public ItemDto update(@PathVariable Long id,
                          @RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody PatchItemDto item) {

        log.info("Получен запрос на обновлении вещи пользователя {} ", userId);

        return ItemMapper.toItemDto(
                itemService.update(id, userId, ItemMapper.toItem(item)));
    }

    @DeleteMapping("/{id}")
    public boolean deleteItem(@PathVariable Long id,
                              @RequestHeader("X-Sharer-User-Id") long userId) {

        log.info("Получен запрос на удалении вещи {} пользователя {} ", id, userId);

        return itemService.delete(userId, id);
    }
}
