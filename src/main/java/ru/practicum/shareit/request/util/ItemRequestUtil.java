package ru.practicum.shareit.request.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class ItemRequestUtil {

    private final ItemRequestRepository itemRequestRepository;

    public ItemRequest getExistItemRequest(Long requestId) {
        if (requestId != null) {
            Optional<ItemRequest> itemRequest =
                    itemRequestRepository.findById(requestId);
            if (itemRequest.isPresent()) {
                return itemRequest.get();
            }
        }
        log.info("Запрос с id: {} не найден.", requestId);
        return null;
    }
}
