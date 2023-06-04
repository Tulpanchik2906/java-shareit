package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // уникальный идентификатор бронирования;

    @Column(name = "start_date")
    private LocalDateTime start; // дата и время начала бронирования;

    @Column(name = "end_date")
    private LocalDateTime end; // дата и время конца бронирования;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item; // вещь, которую пользователь бронирует;

    @ManyToOne
    @JoinColumn(name = "booker_id")
    private User booker; // пользователь, который осуществляет бронирование;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status; // статус бронирования.
}
