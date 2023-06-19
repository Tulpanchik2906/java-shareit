package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import java.util.Map;

@Service
public class ItemRequestClientImp extends BaseClient implements ItemRequestClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClientImp(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    @Override
    public ResponseEntity<Object> create(Long userId, CreateItemRequestDto createItemRequestDto) {
        return post("", userId, createItemRequestDto);
    }

    @Override
    public ResponseEntity<Object> get(Long id, Long userId) {
        return get("/" + id, userId);
    }

    @Override
    public ResponseEntity<Object> findAll(Long userId) {
        return get("", userId);
    }

    @Override
    public ResponseEntity<Object> findAllWithFromAndSize(Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }
}
