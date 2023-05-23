package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select it " +
            "from Item as it " +
            "where (upper(it.name) like upper(concat('%',?1,'%')) " +
            "or upper(it.description) like upper(concat('%',?1,'%'))) " +
            "and (available = true)")
    public List<Item> search(String search);

    @Query("select it " +
            "from Item as it " +
            "JOIN FETCH it.owner as u " +
            "where u.id = ?1")
    public List<Item> findAllByOwnerId(Long userId);

    @Query("select it " +
            "from Item as it " +
            "JOIN FETCH it.owner as u " +
            "where it.id = ?1 and u.id = ?2")
    public List<Item> findByIdAndOwner(Long itemId, Long userId);
}
