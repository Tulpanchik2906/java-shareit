package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.model.Booking;

public interface BookingService {
    Booking get(Long bookingId, Long userId);

    Booking create(Booking booking, Long userId);

    Booking approve(Long bookingId, Long userId, boolean approved);
}
