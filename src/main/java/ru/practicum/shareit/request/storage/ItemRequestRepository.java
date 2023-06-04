package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequesterId(Long userId, Pageable pageable);

    List<ItemRequest> findByRequesterId(Long userId);

    default ItemRequest getExistItemRequest(Long requestId) {
        if (requestId != null) {
            Optional<ItemRequest> itemRequest = findById(requestId);
            if (itemRequest.isPresent()) {
                return itemRequest.get();
            }
        }
        return null;
    }

}
