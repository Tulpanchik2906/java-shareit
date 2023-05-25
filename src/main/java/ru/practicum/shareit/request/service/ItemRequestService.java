package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.ItemRequest;

public interface ItemRequestService {
    ItemRequest create(ItemRequest itemRequest, Long userId);
}
