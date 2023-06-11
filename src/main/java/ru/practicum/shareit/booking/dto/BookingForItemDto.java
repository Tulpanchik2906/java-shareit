package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingForItemDto {
    private Long id; // уникальный идентификатор бронирования;
    private LocalDateTime start; // дата и время начала бронирования;
    private LocalDateTime end; // дата и время конца бронирования;
    private Long bookerId; // id пользователя, который осуществляет бронирование;
    private BookingStatus status; // статус бронирования.
}
