package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    public Item add(Item item);

    public Item get(Long itemId, Long userId);

    public Item get(Long itemId);

    public List<Item> findAll(Long userId);

    public Item update(Item item);

    public List<Item> search(String search);

    public boolean delete(Long id, Long userId);
}
