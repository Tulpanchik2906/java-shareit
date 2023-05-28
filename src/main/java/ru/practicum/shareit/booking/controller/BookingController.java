package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(X_SHARER_USER_ID) Long userId,
                             @Valid @RequestBody BookingCreateDto bookingCreateDto) {
        log.info("Получен запрос на бронирование вещи {} от пользователя {} ",
                bookingCreateDto.getItemId(), userId);
        return BookingMapper.toBookingDto(
                bookingService.create(BookingMapper.toBooking(bookingCreateDto), userId, bookingCreateDto.getItemId()));
    }

    @PatchMapping("/{id}")
    public BookingDto approve(@PathVariable Long id,
                              @RequestHeader(X_SHARER_USER_ID) Long userId,
                              @RequestParam(required = true) boolean approved) {
        log.info("Получен запрос на подтвержение бронирования c id {} " +
                        "от пользователя {} с параметром approved = {} ",
                id, userId, approved);
        return BookingMapper.toBookingDto(
                bookingService.approve(id, userId, approved));
    }

    @GetMapping("/{id}")
    public BookingDto get(@PathVariable Long id,
                          @RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получен запрос на просмотр бронирования с id {} от пользователя с id: {}",
                id, userId);
        return BookingMapper.toBookingDto(bookingService.get(id, userId));
    }

    @GetMapping()
    public List<BookingDto> findAll(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                    @RequestParam(required = false, defaultValue = "ALL") String state) {
        log.info("Получен запрос на получение списка бронирований пользователя {} " +
                        "с пармаетром state: {} ",
                userId, state);

        return bookingService.findAllByBooker(userId, state).stream()
                .map(x -> BookingMapper.toBookingDto(x))
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllByOwner(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                           @RequestParam(required = false, defaultValue = "ALL") String state) {
        log.info("Получен запрос на получение списка бронирований владельца {} " +
                        "с пармаетром state: {} ",
                userId, state);
        return bookingService.findAllByOwner(userId, state).stream()
                .map(x -> BookingMapper.toBookingDto(x))
                .collect(Collectors.toList());
    }
}
