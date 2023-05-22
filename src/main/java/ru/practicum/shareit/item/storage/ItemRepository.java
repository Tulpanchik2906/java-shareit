package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select it " +
            "from Item as it " +
            "where upper(it.name) like upper(concat('%',?1,'%')) " +
            "or upper(it.description) like upper(concat('%',?1,'%'))")
    public List<Item> search(String search);

    public List<Item> findAllByOwner(Long userId);
}
