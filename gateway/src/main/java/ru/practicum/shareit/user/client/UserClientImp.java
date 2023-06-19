package ru.practicum.shareit.user.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.PatchUserDto;

@Service
public class UserClientImp extends BaseClient implements UserClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClientImp(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    @Override
    public ResponseEntity<Object> addUser(CreateUserDto user) {
        return post("", user);
    }

    @Override
    public ResponseEntity<Object> updateUserById(Long id, PatchUserDto user) {
        return patch("/" + id, user);
    }

    @Override
    public ResponseEntity<Object> getUserById(Long id) {
        return get("/" + id);
    }

    @Override
    public ResponseEntity<Object> deleteUserById(Long id) {
        return delete("/" + id);
    }

    @Override
    public ResponseEntity<Object> findAll() {
        return get("");
    }
}
