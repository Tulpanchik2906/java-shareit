package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemStorage itemStorage;

    private final UserStorage userStorage;

    public Item get(Long id, Long userId) {
        if (userStorage.get(userId) == null) {
            throw new NullPointerException("Пользователь с id: " + userId + " не найден.");
        }
        Item item = itemStorage.get(id, userId);

        if (item == null) {
            throw new NullPointerException("Вещь с id: " + id + " не найден.");
        }

        return item;
    }

    public List<Item> findAll(Long userId) {
        return itemStorage.findAll(userId);
    }

    public Item create(Long userId, Item item) {
        if (userStorage.get(userId) == null) {
            throw new NullPointerException("Пользователь с id: " + userId + " не найден.");
        }
        item.setOwner(userStorage.get(userId));
        return itemStorage.add(item);
    }

    public Item update(Long userId, Item item) {
        if (userStorage.get(userId) == null) {
            throw new NullPointerException("Пользователь с id: " + userId + " не найден.");
        }
        item.setOwner(userStorage.get(userId));

        if (itemStorage.get(item.getId(), userId) == null) {
            throw new NullPointerException("Вещь с id: " + item.getId() + " не найден.");
        }

        return itemStorage.update(item);
    }

    public boolean delete(Long id, Long userId) {
        if (userStorage.get(userId) == null) {
            throw new NullPointerException("Пользователь с id: " + userId + " не найден.");
        }

        if (itemStorage.get(id, userId) == null) {
            throw new NullPointerException("Вещь с id: " + id + " не найден.");
        }

        return itemStorage.delete(id, userId);
    }

    public List<Item> searchItems(Long userId, String text) {
        if (userStorage.get(userId) == null) {
            throw new NullPointerException("Пользователь с id: " + userId + " не найден.");
        }
        return itemStorage.search(userId, text);
    }

}
