package ru.practicum.shareit.item.client;

import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.PatchItemDto;

public interface ItemClient {
    public ResponseEntity<Object> findAllByUserId(
            Long userId, Integer from, Integer size);

    public ResponseEntity<Object> getItem(Long id, Long userId);

    public ResponseEntity<Object> search(
            Long userId, String text, Integer from, Integer size);

    public ResponseEntity<Object> add(Long userId, CreateItemDto item);

    public ResponseEntity<Object> addComment(Long id, Long userId, CreateCommentDto comment);

    public ResponseEntity<Object> update(Long id, Long userId, PatchItemDto item);

    public ResponseEntity<Object> deleteItem(Long id, Long userId);

}
