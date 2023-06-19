package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Booking get(Long bookingId, Long userId);

    List<Booking> findAllByBooker(
            Long userId, String bookingState, Integer from, Integer size);

    List<Booking> findAllByOwner(
            Long userId, String bookingState, Integer from, Integer size);

    Booking create(Booking booking, Long userId, Long itemId);

    Booking approve(Long bookingId, Long userId, boolean approved);
}
