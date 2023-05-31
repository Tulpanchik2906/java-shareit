package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest create(ItemRequest itemRequest, Long userId);

    List<ItemRequest> findAllByUserId(Long userId);

    List<ItemRequest> findAllByOffset(Long userId, int from, int size);
}
