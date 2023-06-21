package ru.practicum.shareit.booking.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.ValidationException;

import java.util.Map;

@Service
public class BookingClientImp extends BaseClient implements BookingClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClientImp(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    @Override
    public ResponseEntity<Object> getBookings(long userId, String stateParam, Integer from, Integer size)
            throws ValidationException {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new ValidationException("Unknown state: " + stateParam));
        if (from == null && size == null) {
            return get("?state={state}", userId);
        }

        if (from == null || size == null) {
            throw new ValidationException("Не хватает параметров для формирования списка");
        }

        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }


    @Override
    public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    @Override
    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    @Override
    public ResponseEntity<Object> approve(Long id, Long userId, boolean approved) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );

        return patch("/" + id + "?approved={approved}", userId, parameters, null);

    }

    @Override
    public ResponseEntity<Object> findAllByOwner(Long userId, String state, Integer from, Integer size) {

        BookingState.from(state)
                .orElseThrow(() -> new ValidationException("Unknown state: " + state));

        if (from == null && size == null) {
            return get("/owner?state={state}", userId,
                    Map.of("state", state));
        }

        if (from == null || size == null) {
            throw new ValidationException("Не хватает параметров для формирования списка");
        }

        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }

}