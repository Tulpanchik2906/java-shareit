package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Booking get(Long bookingId, Long userId);

    List<Booking> findAllByBooker(Long userId, BookingState bookingState);

    List<Booking> findAllByOwner(Long userId, BookingState bookingState);

    Booking create(Booking booking, Long userId);

    Booking approve(Long bookingId, Long userId, boolean approved);
}
