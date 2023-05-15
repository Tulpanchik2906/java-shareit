package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;

    private final UserStorage userStorage;

    @Override
    public Item getInfo(Long itemId, Long userId) {
        validExistUser(userId);
        // Просматривать информацию о вещи может любой пользователь
        Item item = itemStorage.get(itemId);

        if (item == null) {

            log.info("Вещь с id: {} не найдена для пользователя: {}.", itemId, userId);

            throw new NotFoundException("Вещь с id: " + itemId +
                    " не найдена для пользователя:" + userId + ".");

        }

        return item;
    }

    @Override
    public List<Item> findAllByUser(Long userId) {
        return itemStorage.findAll(userId);
    }

    @Override
    public Item create(Long userId, Item item) {
        validExistUser(userId);
        item.setOwner(userStorage.get(userId));
        return itemStorage.add(item);
    }

    @Override
    public Item update(Long itemId, Long userId, Item item) {
        validItemByUserAndById(itemId, userId);

        Item oldItem = getInfo(itemId, userId);

        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }

        return itemStorage.update(oldItem);
    }

    @Override
    public boolean delete(Long itemId, Long userId) {
        validItemByUserAndById(itemId, userId);

        return itemStorage.delete(itemId, userId);
    }

    @Override
    public List<Item> searchItems(Long userId, String text) {
        validExistUser(userId);
        return itemStorage.search(text);
    }

    private void validExistUser(Long userId) {
        if (userStorage.get(userId) == null) {
            log.error("Пользователь с id: {} не найден.", userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
    }

    private void validItemByUserAndById(Long itemId, Long userId) {
        validExistUser(userId);
        Item item = itemStorage.get(itemId, userId);

        if (item == null) {

            log.info("Вещь с id: {} не найдена для пользователя: {}.", itemId, userId);

            throw new NotFoundException("Вещь с id: " + itemId +
                    " не найдена для пользователя:" + userId + ".");

        }
    }
}
