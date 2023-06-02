package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //Для проверки пользователя, что он брал в аренду вещь.
    List<Booking> findByBookerIdAndItemIdAndStatusAndStartBefore(
            Long userId, Long itemId, BookingStatus status, LocalDateTime current);

    /*
        Списки бронирований, который делал пользователь.
     */

    // Поиск всех бронирований пользователя (ALL)
    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    // Поиск всех бронирований пользователя (CURRENT)
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
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
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long userId, LocalDateTime curStartTime, LocalDateTime curEndTime);

    // PAST
    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime currentTime);

    // FUTURE
    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime currentTime);

    // Status
    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    // Запросы для поиска последнего бронирования
    // Запрос для последнего бронирования,которое уже закончилось

    List<Booking> findByItemIdAndItemOwnerIdAndStatusAndEndBeforeOrderByEndDesc(
            Long itemId, Long userId, BookingStatus status, LocalDateTime currentTime);

    // Запрос для последнего бронирования,которое еще идет
    List<Booking> findByItemIdAndItemOwnerIdAndStatusAndStartBeforeAndEndAfterOrderByEndDesc(
            Long itemId, Long userId, BookingStatus status,
            LocalDateTime currentTimeStart, LocalDateTime currentTimeEnd);


    // Запросы для следующего бронирования
    List<Booking> findByItemIdAndItemOwnerIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, Long userId, BookingStatus status, LocalDateTime currentTime);

    default void validateExistBooking(Long bookingId) {
        if (findById(bookingId).isEmpty()) {
            throw new NotFoundException("Бронирование с id: " + bookingId + " не найдено.");
        }
    }
}
