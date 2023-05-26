package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item getInfo(Long itemId, Long userId);

    List<Item> findAllByUser(Long userId);

    Item create(Long userId, Item item);

    Item update(Long itemId, Long userId, Item item);

    boolean delete(Long itemId, Long userId);

    List<Item> searchItems(Long userId, String text);

}
