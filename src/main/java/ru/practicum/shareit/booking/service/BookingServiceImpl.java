package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking get(Long bookingId, Long userId) {
        userRepository.getExistUser(userId);
        bookingRepository.validateExistBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();

        boolean isNotFound =
                (booking.getItem().getOwner().getId().compareTo(userId) != 0) &&
                        ((booking.getBooker() == null) ||
                                (booking.getBooker().getId().compareTo(userId) != 0));

        if (isNotFound) {
            throw new NotFoundException("Пользователь с id: " + userId
                    + " не может просматривать бронирование c id : " + bookingId);
        }

        return booking;
    }

    @Override
    public List<Booking> findAllByBooker(
            Long userId, String bookingStateStr, Integer from, Integer size) {
        userRepository.getExistUser(userId);
        BookingState bookingState = getBookingState(bookingStateStr);
        LocalDateTime now = LocalDateTime.now();
        if (from == null && size == null) {
            switch (bookingState) {
                case ALL:
                    return bookingRepository.findByBookerIdOrderByStartDesc(userId);
                case CURRENT:
                    return bookingRepository
                            .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                    userId, now, now);
                case PAST:
                    return bookingRepository
                            .findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                case FUTURE:
                    return bookingRepository
                            .findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                case WAITING:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                case REJECTED:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                default:
                    return null;
            }
        }

        if (from != null && size != null) {
            switch (bookingState) {
                case ALL:
                    return bookingRepository
                            .findByBookerIdOrderByStartDesc(userId, PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case CURRENT:
                    return bookingRepository
                            .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                    userId, now, now, PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case PAST:
                    return bookingRepository
                            .findByBookerIdAndEndBeforeOrderByStartDesc(userId, now,
                                    PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository
                            .findByBookerIdAndStartAfterOrderByStartDesc(userId, now,
                                    PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case WAITING:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING,
                                    PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED,
                                    PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                default:
                    return null;
            }
        }

        throw new RuntimeException("Не хватает параметров для формирования списка");

    }

    @Override
    public List<Booking> findAllByOwner(Long userId, String bookingStateStr,
                                        Integer from, Integer size) {
        userRepository.getExistUser(userId);
        BookingState bookingState = getBookingState(bookingStateStr);
        LocalDateTime now = LocalDateTime.now();
        if (from == null && size == null) {
            switch (bookingState) {
                case ALL:
                    return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
                case CURRENT:
                    return bookingRepository
                            .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                    userId, now, now);
                case PAST:
                    return bookingRepository
                            .findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
                case FUTURE:
                    return bookingRepository
                            .findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
                case WAITING:
                    return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                case REJECTED:
                    return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                default:
                    return null;
            }
        }

        if (from != null && size != null) {
            switch (bookingState) {
                case ALL:
                    return bookingRepository.findByItemOwnerIdOrderByStartDesc(
                                    userId, PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case CURRENT:
                    return bookingRepository
                            .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                    userId, now, now,
                                    PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case PAST:
                    return bookingRepository
                            .findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now,
                                    PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository
                            .findByItemOwnerIdAndStartAfterOrderByStartDesc(
                                    userId, now, PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case WAITING:
                    return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                                    userId, BookingStatus.WAITING, PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                                    userId, BookingStatus.REJECTED, PageRequest.of(from, 1))
                            .stream().limit(size)
                            .collect(Collectors.toList());
                default:
                    return null;
            }
        }
        throw new RuntimeException("Не хватает параметров для формирования списка");
    }

    @Override
    @Transactional
    public Booking create(Booking booking, Long userId, Long itemId) {
        validateTimeBooking(booking.getStart(), booking.getEnd());
        userRepository.getExistUser(userId);
        Item item = itemRepository.getItem(itemId);
        itemRepository.validateAvailable(item.getId());

        if (item.getOwner().getId().compareTo(userId) == 0) {
            throw new NotFoundException("Владелец не может забронировать свою же вещь: " +
                    "userId: " + userId + " itemId: " + itemId);
        }

        booking.setItem(item);
        booking.setBooker(userRepository.getExistUser(userId));
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approve(Long bookingId, Long userId, boolean approved) {
        userRepository.getExistUser(userId);
        bookingRepository.validateExistBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Нельзя подтверждать бронирование" +
                    " со статусом: " + booking.getStatus());
        }
        if (booking.getItem().getOwner().getId().compareTo(userId) != 0) {
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

    private void validateTimeBooking(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new ValidationException("Время конца бронирования не позднее времени начала");
        }
    }

    private BookingState getBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (Exception exception) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
