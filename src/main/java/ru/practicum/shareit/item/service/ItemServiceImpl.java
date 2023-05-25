package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemStorage;
    private final UserRepository userStorage;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Item getInfo(Long itemId, Long userId) {
        validateExistUser(userId);
        // Просматривать информацию о вещи может любой пользователь
        Item item = itemStorage.findById(itemId).get();

        if (item == null) {
            log.info("Вещь с id: {} не найдена для пользователя: {}.", itemId, userId);

            throw new NotFoundException("Вещь с id: " + itemId +
                    " не найдена для пользователя:" + userId + ".");

        }

        return item;
    }

    @Override
    public List<Item> findAllByUser(Long userId) {
        return itemStorage.findAllByOwnerId(userId);
    }

    @Override
    public Item create(Long userId, Item item) {
        validateExistUser(userId);
        item.setOwner(userStorage.findById(userId).get());
        return itemStorage.save(item);
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

        return itemStorage.save(oldItem);
    }

    @Override
    public void delete(Long itemId, Long userId) {
        validateItemByUserAndById(itemId, userId);
        itemStorage.deleteById(itemId);
    }

    @Override
    public List<Item> searchItems(Long userId, String text) {
        validateExistUser(userId);
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemStorage.search(text);
    }

    @Override
    public Comment addComment(Long itemId, Long userId, Comment comment) {
        validateExistUser(userId);
        List<Booking> bookings = new ArrayList<>();
        //TODO: Сделать проверку на userId и itemId
        /*
             //   bookingRepository.findByBookerIdAndByItemId(userId, itemId);
        if (bookings.isEmpty()) {
            log.error("Пользователь с id {} не может оставлять комментарий" +
                    " к вещи с id: {}", userId, itemId);
            throw new ApproveBookingException(
                    "Пользователь с id " + userId + " не может оставлять комментарий" +
                            " к вещи с id: " + itemId);
        }
         */
        Item item = getInfo(itemId, userId);
        comment.setAuthor(userStorage.findById(userId).get());
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    private void validateExistUser(Long userId) {
        if (!userStorage.findById(userId).isPresent()) {
            log.error("Пользователь с id: {} не найден.", userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
    }

    private void validateItemByUserAndById(Long itemId, Long userId) {
        validateExistUser(userId);
        List<Item> item = itemStorage.findByIdAndOwner(itemId, userId);

        if (item.size() == 0) {
            log.info("Вещь с id: {} не найдена для пользователя: {}.", itemId, userId);

            throw new NotFoundException("Вещь с id: " + itemId +
                    " не найдена для пользователя:" + userId + ".");

        }
    }
}
