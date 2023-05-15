package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemInMemoryStorage implements ItemStorage {
    private Map<Long, Map<Long, Item>> items = new HashMap<>();

    private Long id = 0L;

    @Override
    public Item add(Item item) {
        item.setId(generateNextId());
        item = save(item);

        log.info("Добавлена новая вещь с id : {}.", item.getId());

        return item;
    }

    @Override
    public Item get(Long itemId, Long userId) {
        if (items.containsKey(userId)) {
            return items.get(userId).get(itemId);
        } else {
            return null;
        }
    }

    @Override
    public Item get(Long itemId) {
        for (Long userId : items.keySet()) {
            if (items.get(userId).containsKey(itemId)) {
                return items.get(userId).get(itemId);
            }
        }
        return null;
    }

    @Override
    public List<Item> findAll(Long userId) {
        if (items.containsKey(userId)) {
            return items.get(userId).values().stream().collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Item update(Item item) {
        item = save(item);

        log.info("Обновлена вещь с id : {}.", item.getId());

        return item;
    }

    @Override
    public List<Item> search(String search) {
        List<Item> searchItems = new ArrayList<>();

        if (search == null || search.isEmpty()) {
            return searchItems;
        }

        for (Long userId : items.keySet()) {
            for (Long itemId : items.get(userId).keySet()) {
                Item item = items.get(userId).get(itemId);

                if ((item.getAvailable() == true) &&
                        (item.getName().toUpperCase().indexOf(search.toUpperCase()) != -1 ||
                                item.getDescription().toUpperCase().indexOf(search.toUpperCase()) != -1)) {
                    searchItems.add(item);
                }
            }
        }
        return searchItems;
    }

    @Override
    public boolean delete(Long id, Long userId) {
        Map<Long, Item> itemsByUser = items.get(userId);
        if (itemsByUser == null) {
            log.info("Не удалось удалить вещь с id : {}", id);
            return false;
        } else {
            itemsByUser.remove(id);
            items.put(userId, itemsByUser);
            log.info("Удалена вещь с id : {}", id);
            return true;
        }
    }

    private Long generateNextId() {
        id++;
        return id;
    }

    private Item save(Item item) {
        Map<Long, Item> currentItemsByUser;

        if (items.containsKey(item.getOwner().getId())) {
            currentItemsByUser = items.get(item.getOwner().getId());
        } else {
            currentItemsByUser = new HashMap<>();
        }
        currentItemsByUser.put(item.getId(), item);
        items.put(item.getOwner().getId(), currentItemsByUser);

        return get(item.getId(), item.getOwner().getId());
    }
}
