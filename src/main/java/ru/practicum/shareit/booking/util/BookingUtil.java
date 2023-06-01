package ru.practicum.shareit.booking.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
@Component
public class BookingUtil {

    private final BookingRepository bookingRepository;

    public void validateExistBooking(Long bookingId) {
        if (bookingRepository.findById(bookingId).isEmpty()) {
            log.error("Бронирование с id: {} не найдено.", bookingId);
            throw new NotFoundException("Бронирование с id: " + bookingId + " не найдено.");
        }
    }

    public void validateTimeBooking(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new ValidationException("Время конца бронирования не позднее времени начала");
        }
    }

    public BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (Exception exception) {
            log.error("Unknown state: ");
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
