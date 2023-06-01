package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.util.BookingUtil;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.util.ItemUtil;
import ru.practicum.shareit.user.util.UserUtil;
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserUtil userUtil;
    private final ItemUtil itemUtil;
    private final BookingUtil bookingUtil;

    @Override
    public Booking get(Long bookingId, Long userId) {
        userUtil.getExistUser(userId);
        bookingUtil.validateExistBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();

        if ((booking.getItem().getOwner().getId().compareTo(userId) != 0) &&
                ((booking.getBooker() == null) ||
                        (booking.getBooker().getId().compareTo(userId) != 0))) {
            log.error("Пользователь с id: {} не может просматривать " +
                    "бронирование c id : {}", userId, bookingId);

            throw new NotFoundException("Пользователь с id: " + userId
                    + " не может просматривать бронирование c id : " + bookingId);
        }

        return booking;
    }

    @Override
    public List<Booking> findAllByBooker(Long userId, String bookingStateStr) {
        userUtil.getExistUser(userId);
        BookingState bookingState = bookingUtil.getBookingState(bookingStateStr);
        switch (bookingState) {
            case ALL:
                return bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case CURRENT:
                return bookingRepository
                        .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId, LocalDateTime.now(), LocalDateTime.now());
            case PAST:
                return bookingRepository
                        .findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case FUTURE:
                return bookingRepository
                        .findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case WAITING:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default:
                return null;
        }
    }

    @Override
    public List<Booking> findAllByOwner(Long userId, String bookingStateStr) {
        userUtil.getExistUser(userId);
        BookingState bookingState = bookingUtil.getBookingState(bookingStateStr);
        switch (bookingState) {
            case ALL:
                return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT:
                return bookingRepository
                        .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId, LocalDateTime.now(), LocalDateTime.now());
            case PAST:
                return bookingRepository
                        .findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case FUTURE:
                return bookingRepository
                        .findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case WAITING:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default:
                return null;
        }
    }

    @Override
    public Booking create(Booking booking, Long userId, Long itemId) {
        bookingUtil.validateTimeBooking(booking.getStart(), booking.getEnd());
        userUtil.getExistUser(userId);
        Item item = itemUtil.getItem(itemId);
        itemUtil.validateAvailable(item.getId());

        if (item.getOwner().getId().compareTo(userId) == 0) {
            log.error("Владелец не может забронировать свою же вещь: " +
                    "userId: {}, itemId: {}", userId, itemId);
            throw new NotFoundException("Владелец не может забронировать свою же вещь: " +
                    "userId: " + userId + " itemId: " + itemId);
        }

        booking.setItem(item);
        booking.setBooker(userUtil.getExistUser(userId));
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long bookingId, Long userId, boolean approved) {
        userUtil.getExistUser(userId);
        bookingUtil.validateExistBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();
        if (booking.getStatus() != BookingStatus.WAITING) {
            log.error("Нельзя подтверждать бронирование со статусом: {} ",
                    booking.getStatus());
            throw new ValidationException("Нельзя подтверждать бронирование" +
                    " со статусом: " + booking.getStatus());
        }
        if (booking.getItem().getOwner().getId().compareTo(userId) != 0) {
            log.error("Пользователь с id: {} не может подтверждать " +
                    "бронирование c id : {}", userId, bookingId);

            throw new NotFoundException("Пользователь с id: " + userId
                    + " не может подтверждать бронирование c id : " + bookingId);
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(booking);
    }
}
