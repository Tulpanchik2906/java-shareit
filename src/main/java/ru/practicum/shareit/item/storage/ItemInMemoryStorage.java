package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemInMemoryStorage implements ItemStorage {
    private Map<Long, Map<Long, Item>> items = new HashMap<>();

    private Long id = 0l;

    @Override
    public Item add(Item item) {
        item.setId(generateNextId());
        return save(item);
    }

    @Override
    public Item get(Long id, Long userId) {
        if (items.containsKey(userId)) {
            return items.get(userId).get(id);
        } else {
            return null;
        }
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
        // Найти старый userId
        List<Long> userIds = (List<Long>) items.keySet();
        Long oldUserId = -1l;
        for (Long userId : userIds) {
            if (items.get(userId).containsKey(item.getId())) {
                oldUserId = userId;
            }
        }

        delete(item.getId(), oldUserId);

        return save(item);
    }

    @Override
    public List<Item> search(Long userId, String search) {
        List<Item> searchItems = new ArrayList<>();

        for (Long itemId : items.get(userId).keySet()) {
            Item item = items.get(userId).get(itemId);
            if (item.getName().toUpperCase().indexOf(search.toUpperCase()) != -1 ||
                    item.getDescription().toUpperCase().indexOf(search.toUpperCase()) != -1) {
                searchItems.add(item);
            }
        }

        return searchItems;
    }

    @Override
    public boolean delete(Long id, Long userId) {
        Map<Long, Item> itemsByUser = items.get(userId);
        if (itemsByUser == null) {
            return false;
        } else {
            itemsByUser.remove(id);
            items.put(userId, itemsByUser);
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
