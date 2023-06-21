package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClientImp;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClientImp bookingClientImp;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(X_SHARER_USER_ID) long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from") Integer from,
                                              @Positive @RequestParam(name = "size") Integer size) {
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClientImp.getBookings(userId, stateParam, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(X_SHARER_USER_ID) long userId,
                                           @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClientImp.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(X_SHARER_USER_ID) long userId,
                                             @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClientImp.getBooking(userId, bookingId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> approve(@PathVariable Long id,
                                          @RequestHeader(X_SHARER_USER_ID) Long userId,
                                          @RequestParam(required = true) boolean approved) {
        log.info("Получен запрос на подтвержение бронирования c id {} от пользователя {} с параметром approved = {} ",
                id, userId, approved);
        return bookingClientImp.approve(id, userId, approved);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllByOwner(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                                 @RequestParam(required = false, defaultValue = "ALL") String state,
                                                 @PositiveOrZero @RequestParam(required = false) Integer from,
                                                 @Positive @RequestParam(required = false) Integer size) {
        log.info("Получен запрос на получение списка бронирований владельца {} с пармаетром state: {} ",
                userId, state);
        return bookingClientImp.findAllByOwner(userId, state, from, size);
    }
}