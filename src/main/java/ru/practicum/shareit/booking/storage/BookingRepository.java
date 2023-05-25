package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    //Для проверки пользователя, что он брал в аренду вещь.
    /*
    @Query("select b " +
            "from Booking as b " +
            "JOIN FETCH b.booker as u " +
            "JOIN FETCH b.item as it "+
            "where u.id = ?1 and it.id = ?2 ")
    List<Booking> findByBookerIdAndByItemId(Long userId, Long itemId);
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

}
