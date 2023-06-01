package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
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
                                 @Valid @RequestBody CreateItemRequestDto createItemRequestDto) {
        log.info("Получен запрос на вещь от пользователя {} ", userId);

        return RequestMapper.toItemRequestDto(itemRequestService.create(
                RequestMapper.toItemRequest(createItemRequestDto), userId));
    }

    @GetMapping
    public List<ItemRequestDto> findAll(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос на получения списка запросов на вещи от пользователя {} ", userId);

        return itemRequestService.findAllByUserId(userId).stream()
                .map(x -> RequestMapper.toItemRequestDto(x))
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllByFromAndSize(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                                     @RequestParam(required = false, defaultValue = "0")
                                                     @Min(0) int from,
                                                     @RequestParam(required = false, defaultValue = "10")
                                                     @Min(1) int size) {
        log.info("Получен запрос на получения списка запросов на вещи от пользователя {} ", userId);

        return itemRequestService.findAllByOffset(userId, from, size).stream()
                .map(x -> RequestMapper.toItemRequestDto(x))
                .collect(Collectors.toList());
    }
}
