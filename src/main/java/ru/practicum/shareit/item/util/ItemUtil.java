package ru.practicum.shareit.item.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.util.exception.NotAvailableItemException;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class ItemUtil {
    private final ItemRepository itemRepository;

    public void validateAvailable(Long itemId) {
        if (!itemRepository.findById(itemId).get().getAvailable().booleanValue()) {
            log.error("Вещь с id: {} не доступна для бронирования.", itemId);
            throw new NotAvailableItemException("Вещь с id: " + itemId + " не доступна для бронирования.");
        }
    }

    public Item getItem(Long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (!item.isPresent()) {
            log.error("Вещь с id: {} не найден.", itemId);
            throw new NotFoundException("Вещь с id: " + itemId + " не найдена.");
        }
        return item.get();
    }
}
