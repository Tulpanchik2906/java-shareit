package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                 @RequestBody CreateItemRequestDto createItemRequestDto) {
        log.info("Получен запрос на вещь от пользователя {} ", userId);

        return RequestMapper.toItemRequestDto(itemRequestService.create(
                RequestMapper.toItemRequest(createItemRequestDto), userId));
    }

    @GetMapping("/{id}")
    public ItemRequestDto get(@PathVariable Long id,
                              @RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос на получения информации о запросе на вещи {} от пользователя {} ",
                id, userId);
        return RequestMapper.toItemRequestDto(itemRequestService.get(id, userId));
    }

    @GetMapping
    public List<ItemRequestDto> findAll(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос на получения списка запросов на вещи от пользователя {} ", userId);

        return itemRequestService.findAllByRequesterId(userId).stream()
                .map(x -> RequestMapper.toItemRequestDto(x))
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllWithFromAndSize(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                                       @RequestParam(required = false) Integer from,
                                                       @RequestParam(required = false) Integer size) {
        log.info("Получен запрос на получения списка запросов на вещи от пользователя {} ", userId);

        return itemRequestService.findAllByOffset(userId, from, size).stream()
                .map(x -> RequestMapper.toItemRequestDto(x))
                .collect(Collectors.toList());
    }
}
