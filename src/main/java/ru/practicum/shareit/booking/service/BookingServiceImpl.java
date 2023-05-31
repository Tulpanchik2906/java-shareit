package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotAvailableItemException;
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
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

            throw new NotFoundException("Пользователь с id: " + userId
                    + " не может просматривать бронирование c id : " + bookingId);
        }

        return booking;
    }

    @Override
    public List<Booking> findAllByBooker(Long userId, String bookingStateStr) {
        validateExistUser(userId);
        BookingState bookingState = getBookingState(bookingStateStr);
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
        validateExistUser(userId);
        BookingState bookingState = getBookingState(bookingStateStr);
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
    @Transactional
    public Booking create(Booking booking, Long userId, Long itemId) {
        validateTimeBooking(booking.getStart(), booking.getEnd());
        validateExistUser(userId);
        Item item = getItem(itemId);
        validateAvailable(item.getId());

        if (item.getOwner().getId().compareTo(userId) == 0) {
            log.error("Владелец не может забронировать свою же вещь: " +
                    "userId: {}, itemId: {}", userId, itemId);
            throw new NotFoundException("Владелец не может забронировать свою же вещь: " +
                    "userId: " + userId + " itemId: " + itemId);
        }

        booking.setItem(item);
        booking.setBooker(userRepository.getReferenceById(userId));
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approve(Long bookingId, Long userId, boolean approved) {
        validateExistUser(userId);
        validateExistBooking(bookingId);
        Booking booking = bookingRepository.getById(bookingId);
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


    private void validateExistUser(Long userId) {
        if (userId == null || !userRepository.findById(userId).isPresent()) {
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
        if (!itemRepository.findById(itemId).get().getAvailable().booleanValue()) {
            log.error("Вещь с id: {} не доступна для бронирования.", itemId);
            throw new NotAvailableItemException("Вещь с id: " + itemId + " не доступна для бронирования.");
        }
    }

    private void validateTimeBooking(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new ValidationException("Время конца бронирования не позднее времени начала");
        }
    }


    private Item getItem(Long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (!item.isPresent()) {
            log.error("Вещь с id: {} не найден.", itemId);
            throw new NotFoundException("Вещь с id: " + itemId + " не найдена.");
        }
        return item.get();
    }

    private BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (Exception exception) {
            log.error("Unknown state: ");
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
