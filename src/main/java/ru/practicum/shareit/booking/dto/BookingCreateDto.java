package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {

    @Future
    @NotNull
    private LocalDateTime start; // дата и время начала бронирования;
    @NotNull
    @Future
    private LocalDateTime end; // дата и время конца бронирования;
    @NotNull
    private Long itemId; // вещь, которую пользователь бронирует;

}
