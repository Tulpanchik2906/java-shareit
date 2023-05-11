package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    public Item add(Item item);

    public Item get(Long id, Long userId);

    public List<Item> findAll(Long userId);

    public Item update(Item item);

    public List<Item> search(Long userId,String search);

    public boolean delete(Long id, Long userId);
}
