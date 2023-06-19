package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest create(ItemRequest itemRequest, Long userId);

    ItemRequest get(Long requestId, Long userId);

    List<ItemRequest> findAllByRequesterId(Long userId);

    List<ItemRequest> findAllByOffset(Long userId, Integer from, Integer size);
}
