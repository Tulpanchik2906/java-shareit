package ru.practicum.shareit.booking.client;

import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

public interface BookingClient {
    public ResponseEntity<Object> getBookings(long userId, BookingState state, Integer from, Integer size);

    public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto);

    public ResponseEntity<Object> getBooking(long userId, Long bookingId);

    public ResponseEntity<Object> approve(Long id, Long userId, boolean approved);

    public ResponseEntity<Object> findAllByOwner(
            Long userId, String state, Integer from, Integer size);
}
