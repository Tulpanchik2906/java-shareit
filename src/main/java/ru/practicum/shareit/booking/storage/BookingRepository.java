package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //Для проверки пользователя, что он брал в аренду вещь.
    List<Booking> findByBookerIdAndItemId(Long userId, Long itemId);

    /*
        Списки бронирований, который делал пользователь.
     */

    // Поиск всех бронирований пользователя (ALL)
    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    // Поиск всех бронирований пользователя (CURRENT)
    List<Booking> findByBookerIdAndStartAfterAndEndBeforeOrderByStartDesc(
            Long userId, LocalDateTime curStartTime, LocalDateTime curEndTime);

    // Поиск всех бронирований пользователя (PAST)
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime currentTime);

    // Поиск всех бронирований пользователя (FUTURE)
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime currentTime);

    // Поиск всех бронирований пользователя по статусу
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    /*
        Списки бронирований вещей для владельцев вещей.
     */

    // ALL
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long userId);

    // CURRENT
    List<Booking> findByItemOwnerIdAndStartAfterAndEndBeforeOrderByStartDesc(
            Long userId, LocalDateTime curStartTime, LocalDateTime curEndTime);

    // PAST
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime currentTime);

    // FUTURE
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime currentTime);

    // Status
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);


}
