package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.ApproveBookingException;
import ru.practicum.shareit.util.exception.NotAvailableItemException;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking get(Long bookingId, Long userId) {
        validateExistUser(userId);
        validateExistBooking(bookingId);
        Booking booking = bookingRepository.getById(bookingId);

        if ((booking.getItem().getOwner().getId().compareTo(userId) != 0) &&
                ((booking.getBooker() == null) ||
                        (booking.getBooker().getId().compareTo(userId) != 0))) {
            log.error("Пользователь с id: {} не может просматривать " +
                    "бронирование c id : {}", userId, bookingId);

            throw new ApproveBookingException("Пользователь с id: " + userId
                    + " не может просматривать бронирование c id : " + bookingId);
        }

        return booking;
    }

    @Override
    public List<Booking> findAllByBooker(Long userId, BookingState bookingState) {
        validateExistUser(userId);
        switch (bookingState) {
            case ALL:
                return bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case CURRENT:
                return bookingRepository
                        .findByBookerIdAndStartAfterAndEndBeforeOrderByStartDesc(
                                userId, LocalDateTime.now(), LocalDateTime.now());
            case PAST:
                bookingRepository
                        .findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case FUTURE:
                bookingRepository
                        .findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case WAITING:
                bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED:
                bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default:
                return null;
        }
    }

    @Override
    public Booking create(Booking booking, Long userId) {
        validateExistUser(userId);
        validateAvailable(booking.getItem().getId());

        booking.setBooker(userRepository.getReferenceById(userId));
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long bookingId, Long userId, boolean approved) {
        validateExistUser(userId);
        validateExistBooking(bookingId);
        Booking booking = bookingRepository.getById(bookingId);
        if (booking.getItem().getOwner().getId().compareTo(userId) != 0) {
            log.error("Пользователь с id: {} не может подтверждать " +
                    "бронирование c id : {}", userId, bookingId);

            throw new ApproveBookingException("Пользователь с id: " + userId
                    + " не может подтверждать бронирование c id : " + bookingId);
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(booking);
    }


    private void validateExistUser(Long userId) {
        if (!userRepository.findById(userId).isPresent()) {
            log.error("Пользователь с id: {} не найден.", userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
    }

    private void validateExistBooking(Long bookingId) {
        if (!bookingRepository.findById(bookingId).isPresent()) {
            log.error("Бронирование с id: {} не найдено.", bookingId);
            throw new NotFoundException("Бронирование с id: " + bookingId + " не найдено.");
        }
    }

    private void validateAvailable(Long itemId) {
        if (itemRepository.findById(itemId).get().getAvailable().booleanValue() == false) {
            log.error("Вещь с id: {} не доступна для бронирования.", itemId);
            throw new NotAvailableItemException("Вещь с id: " + itemId + " не доступна для бронирования.");
        }
    }

}
