package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.util.UserUtil;
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
    private final UserUtil userUtil;

    @Override
    public Item getInfo(Long itemId, Long userId) {
        userUtil.getExistUser(userId);
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
    public List<Item> findAllByUser(Long userId) {
        return setAddParamToItemList(itemStorage.findByOwnerId(userId), userId);
    }

    @Override
    public Item create(Long userId, Item item) {
        userUtil.getExistUser(userId);
        item.setOwner(userUtil.getExistUser(userId));
        Item res = itemStorage.save(item);

        return getInfo(res.getId(), res.getOwner().getId());
    }

    @Override
    public Item update(Long itemId, Long userId, Item item) {
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
    public List<Item> searchItems(Long userId, String text) {
        userUtil.getExistUser(userId);
        // В случае пустого параметра text вернуть пустой список
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return setAddParamToItemList(itemStorage.search(text), userId);
    }

    @Override
    public Comment addComment(Long itemId, Long userId, Comment comment) {
        userUtil.getExistUser(userId);

        List<Booking> bookings = bookingRepository
                .findByBookerIdAndItemIdAndStatusAndStartBefore(
                        userId, itemId, BookingStatus.APPROVED,
                        LocalDateTime.now());
        if (bookings.isEmpty()) {
            log.error("Пользователь с id {} не может оставлять комментарий" +
                    " к вещи с id: {}", userId, itemId);
            throw new ValidationException(
                    "Пользователь с id " + userId + " не может оставлять комментарий" +
                            " к вещи с id: " + itemId);
        }

        Item item = getInfo(itemId, userId);
        comment.setAuthor(userUtil.getExistUser(userId));
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    private void validateItemByUserAndById(Long itemId, Long userId) {
        userUtil.getExistUser(userId);
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
}


