package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.util.exception.NotAvailableItemException;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select it " +
            "from Item as it " +
            "where (upper(it.name) like upper(concat('%',?1,'%')) " +
            "or upper(it.description) like upper(concat('%',?1,'%'))) " +
            "and (it.available = true)")
    public List<Item> search(String search);

    @Query("select it " +
            "from Item as it " +
            "where (upper(it.name) like upper(concat('%',?1,'%')) " +
            "or upper(it.description) like upper(concat('%',?1,'%'))) " +
            "and (it.available = true)")
    public List<Item> search(String search, Pageable pageable);

    public List<Item> findByOwnerId(Long userId);

    public List<Item> findByOwnerId(Long userId, Pageable pageable);

    public List<Item> findByIdAndOwnerId(Long itemId, Long userId);

    public List<Item> findByRequestId(Long requestId);

    default Item getItem(Long itemId) {
        Optional<Item> item = findById(itemId);
        if (!item.isPresent()) {
            throw new NotFoundException("Вещь с id: " + itemId + " не найдена.");
        }
        return item.get();
    }

    default void validateAvailable(Long itemId) {
        if (!findById(itemId).get().getAvailable().booleanValue()) {
            throw new NotAvailableItemException("Вещь с id: " + itemId + " не доступна для бронирования.");
        }
    }
}
