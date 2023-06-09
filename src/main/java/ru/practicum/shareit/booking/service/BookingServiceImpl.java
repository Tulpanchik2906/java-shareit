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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.PageUtil;
import ru.practicum.shareit.util.exception.NotAvailableItemException;
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
        getUser(userId);
        Booking booking = getBookingFromRepo(bookingId);

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
        getUser(userId);
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
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                            userId, BookingStatus.WAITING);
                case REJECTED:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                            userId, BookingStatus.REJECTED);
                default:
                    return null;
            }
        }

        if (from != null && size != null) {
            switch (bookingState) {
                case ALL:
                    return getListWithParamsByBooker(from, size, BookingState.ALL, userId);
                case CURRENT:
                    return getListWithParamsByBooker(from, size, BookingState.CURRENT, userId);
                case PAST:
                    return getListWithParamsByBooker(from, size, BookingState.PAST, userId);
                case FUTURE:
                    return getListWithParamsByBooker(from, size, BookingState.FUTURE, userId);
                case WAITING:
                    return getListWithParamsByBooker(from, size, BookingState.WAITING, userId);
                case REJECTED:
                    return getListWithParamsByBooker(from, size, BookingState.REJECTED, userId);
                default:
                    return null;
            }
        }

        throw new ValidationException("Не хватает параметров для формирования списка");

    }

    @Override
    public List<Booking> findAllByOwner(Long userId, String bookingStateStr,
                                        Integer from, Integer size) {
        getUser(userId);
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
                    return getListWithParamsByOwner(from, size, BookingState.ALL, userId);
                case CURRENT:
                    return getListWithParamsByOwner(from, size, BookingState.CURRENT, userId);
                case PAST:
                    return getListWithParamsByOwner(from, size, BookingState.PAST, userId);
                case FUTURE:
                    return getListWithParamsByOwner(from, size, BookingState.FUTURE, userId);
                case WAITING:
                    return getListWithParamsByOwner(from, size, BookingState.WAITING, userId);
                case REJECTED:
                    return getListWithParamsByOwner(from, size, BookingState.REJECTED, userId);
                default:
                    return null;
            }
        }
        throw new ValidationException("Не хватает параметров для формирования списка");
    }

    @Override
    @Transactional
    public Booking create(Booking booking, Long userId, Long itemId) {
        validateTimeBooking(booking.getStart(), booking.getEnd());
        User user = getUser(userId);
        Item item = getItem(itemId);
        validateAvailable(item.getId());

        if (item.getOwner().getId().compareTo(userId) == 0) {
            throw new NotFoundException("Владелец не может забронировать свою же вещь: " +
                    "userId: " + userId + " itemId: " + itemId);
        }

        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approve(Long bookingId, Long userId, boolean approved) {
        getUser(userId);
        Booking booking = getBookingFromRepo(bookingId);

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

    private List getListWithParamsByBooker(
            int from, int size, BookingState state, Long userId) {
        // Получить номер страницы, с которой взять данные
        int startPage = PageUtil.getStartPage(from, size);

        if (PageUtil.isTwoSite(from, size)) {
            // Получить данные с первой страницы
            List<Booking> list = getListWithParamsByBookerByState1Page(state, userId, startPage, size);
            // Добавить данные со второй страницы
            list.addAll(getListWithParamsByBookerByState1Page(state, userId, startPage + 1, size));
            // Отсечь лишние данные сверху удалением из листа до нужного id,
            // а потом сделать отсечение через функцию limit
            return PageUtil.getPageListForTwoPage(list,
                    PageUtil.getStartFrom(from, size), size);
        } else {
            return (List) getListWithParamsByBookerByState1Page(state, userId, startPage, size)
                    .stream().limit(size)
                    .collect(Collectors.toList());
        }
    }

    private List getListWithParamsByOwner(
            int from, int size, BookingState state, Long userId) {
        // Получить номер страницы, с которой взять данные
        int startPage = PageUtil.getStartPage(from, size);

        if (PageUtil.isTwoSite(from, size)) {
            // Получить данные с первой страницы
            List<Booking> list = getListWithParamsByOwnerByState1Page(state, userId, startPage, size);
            // Добавить данные со второй страницы
            list.addAll(getListWithParamsByOwnerByState1Page(state, userId, startPage + 1, size));
            // Отсечь лишние данные сверху удалением из листа до нужного id,
            // а потом сделать отсечение через функцию limit
            return PageUtil.getPageListForTwoPage(list,
                    PageUtil.getStartFrom(from, size), size);
        } else {
            return (List) getListWithParamsByOwnerByState1Page(state, userId, startPage, size)
                    .stream().limit(size)
                    .collect(Collectors.toList());
        }
    }

    private List getListWithParamsByOwnerByState1Page(
            BookingState bookingState, Long userId, int startPage, int size) {
        LocalDateTime now = LocalDateTime.now();

        switch (bookingState) {
            case ALL:
                // Получить данные с первой страницы
                List<Booking> list =
                        bookingRepository
                                .findByItemOwnerIdOrderByStartDesc(userId, PageRequest.of(startPage, size));
                return list;
            case CURRENT:
                list = bookingRepository
                        .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId, now, now, PageRequest.of(startPage, size));
                return list;
            case PAST:
                list = bookingRepository
                        .findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                                userId, now, PageRequest.of(startPage, size));
                return list;
            case FUTURE:
                list = bookingRepository
                        .findByItemOwnerIdAndStartAfterOrderByStartDesc(
                                userId, now, PageRequest.of(startPage, size));
                return list;
            case WAITING:
                list = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING, PageRequest.of(startPage, size));
                return list;
            case REJECTED:
                list = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED, PageRequest.of(startPage, size));
                return list;
            default:
                return null;
        }
    }

    private List getListWithParamsByBookerByState1Page(
            BookingState bookingState, Long userId, int startPage, int size) {
        LocalDateTime now = LocalDateTime.now();

        switch (bookingState) {
            case ALL:
                // Получить данные с первой страницы
                List<Booking> list =
                        bookingRepository
                                .findByBookerIdOrderByStartDesc(userId,
                                        PageRequest.of(startPage, size));
                return list;
            case CURRENT:
                list = bookingRepository
                        .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId, now, now, PageRequest.of(startPage, size));
                return list;
            case PAST:
                list = bookingRepository
                        .findByBookerIdAndEndBeforeOrderByStartDesc(
                                userId, now, PageRequest.of(startPage, size));
                ;
                return list;
            case FUTURE:
                list = bookingRepository
                        .findByBookerIdAndStartAfterOrderByStartDesc(
                                userId, now, PageRequest.of(startPage, size));
                return list;
            case WAITING:
                list = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING, PageRequest.of(startPage, size));
                return list;
            case REJECTED:
                list = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED, PageRequest.of(startPage, size));
                return list;
            default:
                return null;
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id: " + userId + " не найден."));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Вещь с id: " + itemId + " не найдена."));
    }

    private void validateAvailable(Long itemId) {
        if (!itemRepository.findById(itemId).get().getAvailable().booleanValue()) {
            throw new NotAvailableItemException(
                    "Вещь с id: " + itemId + " не доступна для бронирования.");
        }
    }

    private Booking getBookingFromRepo(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с id: " + bookingId + " не найдено."));
    }
}
