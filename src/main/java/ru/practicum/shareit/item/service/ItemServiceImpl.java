package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.PageUtil;
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemStorage;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public Item getInfo(Long itemId, Long userId) {
        getUser(userId);
        // Просматривать информацию о вещи может любой пользователь
        Optional<Item> itemOpt = itemStorage.findById(itemId);

        if (!itemOpt.isPresent()) {
            log.info("Вещь с id: {} не найдена для пользователя: {}.", itemId, userId);

            throw new NotFoundException("Вещь с id: " + itemId +
                    " не найдена для пользователя:" + userId + ".");

        }

        return setComments(setBookingsInfo(itemOpt.get(), userId));
    }

    @Override
    public List<Item> findAllByUser(Long userId, Integer from, Integer size) {
        getUser(userId);
        if (from == null && size == null) {
            return setAddParamToItemList(itemStorage.findByOwnerId(userId), userId);
        } else if (from == null || size == null) {
            throw new ValidationException("Не хватает параметров для формирования списка");
        } else {
            if (PageUtil.isTwoSite(from, size)) {
                // Получить номер страницы, с которой взять данные
                int startPage = PageUtil.getStartPage(from, size);
                // Получить данные с первой страницы
                List<Item> list = itemStorage.findByOwnerId(userId, PageRequest.of(startPage, size));
                // Получить данные со второй страницы
                list.addAll(itemStorage.findByOwnerId(userId, PageRequest.of(startPage + 1, size)));
                // Отсечь лишние данные сверху удалением из листа до нужного id,
                // а потом сделать отсечение через функцию limit
                return setAddParamToItemList(
                        PageUtil.getPageListForTwoPage(
                                list, PageUtil.getStartFrom(from, size), size), userId);
            } else {
                return setAddParamToItemList(itemStorage
                        .findByOwnerId(userId, PageRequest.of(PageUtil.getStartPage(from, size), size))
                        .stream().limit(size)
                        .collect(Collectors.toList()), userId);
            }
        }
    }

    @Override
    @Transactional
    public Item create(Long userId, Long requestId, Item item) {
        item.setOwner(getUser(userId));
        item.setRequest(getExistItemRequest(requestId));

        Item res = itemStorage.save(item);

        return getInfo(res.getId(), res.getOwner().getId());
    }

    @Override
    @Transactional
    public Item update(Long itemId, Long userId, Long requestId, Item item) {
        validateItemByUserAndById(itemId, userId);

        Item oldItem = getInfo(itemId, userId);

        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }

        if (requestId != null) {
            oldItem.setRequest(getExistItemRequest(requestId));
        }

        Item res = itemStorage.save(oldItem);

        return getInfo(res.getId(), res.getOwner().getId());
    }

    @Override
    @Transactional
    public void delete(Long itemId, Long userId) {
        validateItemByUserAndById(itemId, userId);
        itemStorage.deleteById(itemId);
    }

    @Override
    public List<Item> searchItems(Long userId, String text, Integer from, Integer size) {
        getUser(userId);
        // В случае пустого параметра text вернуть пустой список
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        if (from == null && size == null) {
            return setAddParamToItemList(itemStorage.search(text), userId);
        } else if (from == null || size == null) {
            throw new ValidationException("Не хватает параметров для формирования списка");
        } else {
            // Если все нужные данные находятся на 2-х страницах
            if (PageUtil.isTwoSite(from, size)) {
                // Получить номер страницы, с которой взять данные
                int startPage = PageUtil.getStartPage(from, size);
                // Получить данные с первой страницы
                List<Item> list = itemStorage.search(text, PageRequest.of(startPage, size));
                // Получить данные со второй страницы
                list.addAll(itemStorage.search(text, PageRequest.of(startPage + 1, size)));
                // Отсечь лишние данные сверху удалением из листа до нужного id,
                // а потом сделать отсечение через функцию limit
                return setAddParamToItemList(
                        PageUtil.getPageListForTwoPage(
                                list, PageUtil.getStartFrom(from, size), size), userId);
            } else {
                return setAddParamToItemList(itemStorage
                        .search(text, PageRequest.of(PageUtil.getStartPage(from, size), size))
                        .stream().limit(size)
                        .collect(Collectors.toList()), userId);
            }
        }
    }

    @Override
    @Transactional
    public Comment addComment(Long itemId, Long userId, Comment comment) {
        User user = getUser(userId);

        List<Booking> bookings = bookingRepository
                .findByBookerIdAndItemIdAndStatusAndStartBefore(
                        userId, itemId, BookingStatus.APPROVED,
                        LocalDateTime.now());
        if (bookings.isEmpty()) {
            throw new ValidationException(
                    "Пользователь с id " + userId + " не может оставлять комментарий" +
                            " к вещи с id: " + itemId);
        }

        Item item = getInfo(itemId, userId);
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    private void validateItemByUserAndById(Long itemId, Long userId) {
        getUser(userId);
        List<Item> item = itemStorage.findByIdAndOwnerId(itemId, userId);

        if (item.size() == 0) {
            log.info("Вещь с id: {} не найдена для пользователя: {}.", itemId, userId);

            throw new NotFoundException("Вещь с id: " + itemId +
                    " не найдена для пользователя:" + userId + ".");

        }
    }

    private Item setBookingsInfo(Item item, Long userId) {
        // Поиск последнего бронирования (уже закончилось)
        log.info("Текущее время {}", LocalDateTime.now());

        List<Booking> findLastBooking = bookingRepository
                .findByItemIdAndItemOwnerIdAndStatusAndEndBeforeOrderByEndDesc(
                        item.getId(), userId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!findLastBooking.isEmpty()) {
            item.setLastBooking(findLastBooking.get(0));
        }

        // Поиск последнего бронирования (текущее)
        List<Booking> findCurrentBooking = bookingRepository
                .findByItemIdAndItemOwnerIdAndStatusAndStartBeforeAndEndAfterOrderByEndDesc(
                        item.getId(), userId, BookingStatus.APPROVED,
                        LocalDateTime.now(), LocalDateTime.now());

        if (!findCurrentBooking.isEmpty()) {
            item.setLastBooking(findCurrentBooking.get(0));
        }

        // Поиск следующего бронирования
        List<Booking> findNextBooking = bookingRepository
                .findByItemIdAndItemOwnerIdAndStatusAndStartAfterOrderByStartAsc(
                        item.getId(), userId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!findNextBooking.isEmpty()) {
            item.setNextBooking(findNextBooking.get(0));
        }

        return item;
    }

    private Item setComments(Item item) {
        item.setComments(commentRepository.findByItemId(item.getId()));
        return item;
    }

    private List<Item> setAddParamToItemList(List<Item> items, Long userId) {
        return items.stream()
                .map(x -> setComments(setBookingsInfo(x, userId)))
                .collect(Collectors.toList());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id: " + userId + " не найден."));
    }

    private ItemRequest getExistItemRequest(Long requestId) {
        if (requestId != null) {
            Optional<ItemRequest> itemRequest = itemRequestRepository.findById(requestId);
            if (itemRequest.isPresent()) {
                return itemRequest.get();
            }
        }
        return null;
    }

}


