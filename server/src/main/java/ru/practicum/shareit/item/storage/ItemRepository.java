package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

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

    public List<Item> findByOwnerIdOrderByIdAsc(Long userId);

    public List<Item> findByOwnerIdOrderByIdAsc(Long userId, Pageable pageable);

    public List<Item> findByIdAndOwnerIdOrderByIdAsc(Long itemId, Long userId);

    public List<Item> findByRequestId(Long requestId);

}
