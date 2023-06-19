package ru.practicum.shareit.item.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.PatchItemDto;

import java.util.Map;

@Service
public class ItemClientImp extends BaseClient implements ItemClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClientImp(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    @Override
    public ResponseEntity<Object> findAllByUserId(
            Long userId, Integer from, Integer size) {
        if (from == null && size == null) {
            return get("", userId);
        }

        if (from == null || size == null) {
            throw new ValidationException("Не хватает параметров для формирования списка");
        }

        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );

        return get("?from={from}&size={size}", userId, parameters);
    }

    @Override
    public ResponseEntity<Object> getItem(Long id, Long userId) {
        return get("/" + id, userId);
    }

    @Override
    public ResponseEntity<Object> search(
            Long userId, String text, Integer from, Integer size) {

        if (from == null && size == null) {
            return get("/search?text={text}", userId,
                    Map.of("text", text));
        }

        if (from == null || size == null) {
            throw new ValidationException("Не хватает параметров для формирования списка");
        }

        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}",
                userId, parameters);
    }

    @Override
    public ResponseEntity<Object> add(Long userId, CreateItemDto item) {
        return post("", userId, item);
    }

    @Override
    public ResponseEntity<Object> addComment(
            Long id, Long userId, CreateCommentDto comment) {
        return post("/" + id + "/comment", userId, comment);
    }

    @Override
    public ResponseEntity<Object> update(
            Long id, Long userId, PatchItemDto item) {
        return patch("/" + id, userId, item);
    }

    @Override
    public ResponseEntity<Object> deleteItem(Long id, Long userId) {
        return delete("/" + id, userId);
    }
}
