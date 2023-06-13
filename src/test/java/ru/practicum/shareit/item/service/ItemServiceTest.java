package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.TestUtil;
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
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase
public class ItemServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private Item item;

    @BeforeEach
    public void beforeEach() {
        owner = addUser();
        item = getDefaultItem();
    }

    @AfterEach
    public void afterEach() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    /*
        Тесты на метод  Item create(Long userId, Long requestId, Item item);
     */

    @Test
    public void testCreateItemWithOutRequestSuccess() {
        Item itemRes = itemService.create(owner.getId(), null, item);

        Assertions.assertEquals(item.getName(), itemRes.getName());
        Assertions.assertEquals(item.getDescription(), itemRes.getDescription());
        Assertions.assertEquals(item.getAvailable(), itemRes.getAvailable());
        Assertions.assertEquals(null, itemRes.getRequest());
        Assertions.assertNotNull(itemRes.getId());
    }

    @Test
    public void testCreateItemWithRequestSuccess() {
        User requester = addUser();
        ItemRequest itemRequest = addItemRequest(requester);
        Item itemRes = itemService.create(owner.getId(), itemRequest.getId(), item);

        Assertions.assertEquals(item.getName(), itemRes.getName());
        Assertions.assertEquals(item.getDescription(), itemRes.getDescription());
        Assertions.assertEquals(item.getAvailable(), itemRes.getAvailable());
        Assertions.assertEquals(itemRequestRepository.findById(itemRequest.getId()).get(),
                itemRes.getRequest());
        Assertions.assertEquals(item.getId(), itemRes.getId());
    }

    @Test
    public void testCreateItemFailedNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.create(owner.getId() + 1, null, getDefaultItem()));
    }

    @Test
    public void testCreateItemFailedNoRequest() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.create(owner.getId(), 1L, getDefaultItem()));
    }

    /*
        Тесты на метод Item update(Long itemId, Long userId, Long requestId, Item item)
     */
    @Test
    public void testUpdateSuccess() {
        User requester = addUser();
        ItemRequest itemRequest = addItemRequest(requester);
        Item createItem = itemService.create(owner.getId(), null, item);
        Item patchItem = getDefaultUpdateItem();

        Item updatedItem = itemService.update(createItem.getId(),
                createItem.getOwner().getId(), itemRequest.getId(), patchItem);

        Assertions.assertEquals(patchItem.getName(), updatedItem.getName());
        Assertions.assertEquals(patchItem.getDescription(), updatedItem.getDescription());
        Assertions.assertEquals(createItem.getAvailable(), updatedItem.getAvailable());
        Assertions.assertEquals(itemRequestRepository.findById(itemRequest.getId()).get(),
                updatedItem.getRequest());
        Assertions.assertNotNull(createItem.getId());
    }

    @Test
    public void testUpdateWithNullFieldsSuccess() {
        User requester = addUser();
        ItemRequest itemRequest = addItemRequest(requester);
        Item createItem = itemService.create(owner.getId(), itemRequest.getId(), item);
        Item patchItem = getDefaultUpdateItem();
        patchItem.setName(null);
        patchItem.setAvailable(null);
        patchItem.setDescription(null);


        Item updatedItem = itemService.update(createItem.getId(),
                createItem.getOwner().getId(), null, patchItem);

        Assertions.assertEquals(createItem.getName(), updatedItem.getName());
        Assertions.assertEquals(createItem.getDescription(), updatedItem.getDescription());
        Assertions.assertEquals(createItem.getAvailable(), updatedItem.getAvailable());
        Assertions.assertEquals(itemRequestRepository.findById(itemRequest.getId()).get(),
                updatedItem.getRequest());
        Assertions.assertNotNull(createItem.getId());
    }

    @Test
    public void testUpdateItemFailedNoUser() {
        Item createItem = itemService.create(owner.getId(), null, item);
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.update(createItem.getId(),
                        owner.getId() + 1, null, getDefaultItem()));
    }

    @Test
    public void testUpdateItemFailedNoItem() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.update(1L,
                        owner.getId(), null, getDefaultItem()));
    }

    /*
        Тесты на метод getInfo()
     */

    @Test
    public void testGetInfoByOwnerSuccess() {
        User requester = addUser();
        User booker = addUser();
        ItemRequest itemRequest = addItemRequest(requester);
        Item itemCreate = itemService.create(owner.getId(), itemRequest.getId(), item);
        Booking lastBooking = bookingRepository.save(
                getDefaultBooking(itemCreate, booker, LocalDateTime.now().minusDays(1)));
        Booking nextBooking = bookingRepository.save(
                getDefaultBooking(itemCreate, booker, LocalDateTime.now().plusDays(12)));


        Item itemRes = itemService.getInfo(itemCreate.getId(), owner.getId());

        Assertions.assertEquals(itemCreate.getName(), itemRes.getName());
        Assertions.assertEquals(itemCreate.getDescription(), itemRes.getDescription());
        Assertions.assertEquals(itemCreate.getAvailable(), itemRes.getAvailable());
        Assertions.assertEquals(itemRequestRepository.findById(itemRequest.getId()).get(),
                itemRes.getRequest());
        Assertions.assertEquals(itemCreate.getId(), itemRes.getId());
        Assertions.assertEquals(bookingRepository.findById(lastBooking.getId()).get(),
                itemRes.getLastBooking());
        Assertions.assertEquals(bookingRepository.findById(nextBooking.getId()).get(),
                itemRes.getNextBooking());
        Assertions.assertTrue(itemRes.getComments().isEmpty());
    }

    @Test
    public void testGetInfoByBookerSuccess() {
        User requester = addUser();
        User booker = addUser();
        ItemRequest itemRequest = addItemRequest(requester);
        Item itemCreate = itemService.create(owner.getId(), itemRequest.getId(), item);
        Booking lastBooking = bookingRepository.save(
                getDefaultBooking(itemCreate, booker, LocalDateTime.now().minusDays(1)));
        Booking nextBooking = bookingRepository.save(
                getDefaultBooking(itemCreate, booker, LocalDateTime.now().plusDays(12)));


        Item itemRes = itemService.getInfo(itemCreate.getId(), booker.getId());

        Assertions.assertEquals(itemCreate.getName(), itemRes.getName());
        Assertions.assertEquals(itemCreate.getDescription(), itemRes.getDescription());
        Assertions.assertEquals(itemCreate.getAvailable(), itemRes.getAvailable());
        Assertions.assertEquals(itemRequest.getId(), itemRes.getRequest().getId());
        Assertions.assertEquals(itemCreate.getId(), itemRes.getId());
        Assertions.assertNull(itemRes.getLastBooking());
        Assertions.assertNull(itemRes.getNextBooking());
        Assertions.assertTrue(itemRes.getComments().isEmpty());
    }

    @Test
    public void testGetInfoByBookerWithFinishedBookingsSuccess() {
        User requester = addUser();
        User booker = addUser();
        ItemRequest itemRequest = addItemRequest(requester);
        Item itemCreate = itemService.create(owner.getId(), itemRequest.getId(), item);
        Booking lastBooking = bookingRepository.save(
                getDefaultBooking(itemCreate, booker, LocalDateTime.now().minusDays(100)));


        Item itemRes = itemService.getInfo(itemCreate.getId(), owner.getId());

        Assertions.assertEquals(itemCreate.getName(), itemRes.getName());
        Assertions.assertEquals(itemCreate.getDescription(), itemRes.getDescription());
        Assertions.assertEquals(itemCreate.getAvailable(), itemRes.getAvailable());
        Assertions.assertEquals(itemRequestRepository.findById(itemRequest.getId()).get(),
                itemRes.getRequest());
        Assertions.assertEquals(itemCreate.getId(), itemRes.getId());
        Assertions.assertEquals(bookingRepository.findById(lastBooking.getId()).get(),
                itemRes.getLastBooking());
        Assertions.assertNull(itemRes.getNextBooking());
        Assertions.assertTrue(itemRes.getComments().isEmpty());
    }

    @Test
    public void testGetInfoWithCommentsSuccess() {
        User requester = addUser();
        User booker = addUser();
        ItemRequest itemRequest = addItemRequest(requester);
        Item itemCreate = itemService.create(owner.getId(), itemRequest.getId(), item);
        Booking lastBooking = bookingRepository.save(
                getDefaultBooking(itemCreate, booker, LocalDateTime.now().minusDays(1)));
        itemService.addComment(itemCreate.getId(), booker.getId(), getNewComment());
        itemService.addComment(itemCreate.getId(), booker.getId(), getNewComment());


        Item itemRes = itemService.getInfo(itemCreate.getId(), booker.getId());

        Assertions.assertEquals(itemCreate.getName(), itemRes.getName());
        Assertions.assertEquals(itemCreate.getDescription(), itemRes.getDescription());
        Assertions.assertEquals(itemCreate.getAvailable(), itemRes.getAvailable());
        Assertions.assertEquals(itemRequest.getId(), itemRes.getRequest().getId());
        Assertions.assertEquals(itemCreate.getId(), itemRes.getId());
        Assertions.assertNull(itemRes.getLastBooking());
        Assertions.assertNull(itemRes.getNextBooking());
        Assertions.assertEquals(2, itemRes.getComments().size());
    }

    @Test
    public void testGetInfoFailedNoItem() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.getInfo(1L, owner.getId()));
    }

    @Test
    public void testGetInfoFailedNoUser() {
        Item createItem = itemService.create(owner.getId(), null, item);
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.getInfo(createItem.getId(), owner.getId() + 1));
    }

    /*
        Тесты на метод List<Item> findAllByOwner(Long userId, Integer from, Integer size);
     */

    @Test
    public void testFindAllByOwnerWithOutPage() {
        User owner2 = addUser();
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner2.getId(), null, getDefaultItem());

        Assertions.assertEquals(2, itemService.findAllByOwner(
                owner.getId(), null, null).size());
    }

    @Test
    public void testFindAllByOwnerWithPageFirstElement() {
        User owner2 = addUser();
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner2.getId(), null, getDefaultItem());

        Assertions.assertEquals(2, itemService.findAllByOwner(
                owner.getId(), 0, 5).size());
    }

    @Test
    public void testFindAllByOwnerWithOutPageEmptyList() {
        Assertions.assertTrue(itemService.findAllByOwner(
                owner.getId(), null, null).isEmpty());
    }

    @Test
    public void testFindAllByOwnerWithPageEmptyList() {
        Assertions.assertTrue(itemService.findAllByOwner(
                owner.getId(), 0, 10).isEmpty());
    }


    @Test
    public void testFindAllByOwnerWithPageMiddleElement() {
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());

        Assertions.assertEquals(2, itemService.findAllByOwner(
                owner.getId(), 1, 5).size());
    }

    @Test
    public void testFindAllByOwnerWithPageTwoPage() {
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());

        Assertions.assertEquals(2, itemService.findAllByOwner(
                owner.getId(), 1, 2).size());
    }

    @Test
    public void testFindAllByOwnerWithPageLastElement() {
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());

        Assertions.assertEquals(1, itemService.findAllByOwner(
                owner.getId(), 2, 5).size());
    }

    @Test
    public void testFindAllByOwnerNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.findAllByOwner(owner.getId() + 1, null, null));
    }

    @Test
    public void testFindAllByOwnerNoParamFrom() {
        Assertions.assertThrows(ValidationException.class,
                () -> itemService.findAllByOwner(owner.getId(), null, 1));
    }

    @Test
    public void testFindAllByOwnerNoParamSize() {
        Assertions.assertThrows(ValidationException.class,
                () -> itemService.findAllByOwner(owner.getId(), 1, null));
    }

    /*
        Тесты для метода: delete(Long itemId, Long userId);
     */

    @Test
    public void testDeleteSuccess() {
        Item createdItem = itemService.create(owner.getId(), null, item);
        itemService.delete(createdItem.getId(), owner.getId());

        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.getInfo(createdItem.getId(), owner.getId()));
    }

    @Test
    public void testDeleteItemFailedNoItem() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.delete(1L, owner.getId()));
    }

    @Test
    public void testDeleteItemFailedNoUser() {
        Item createdItem = itemService.create(owner.getId(), null, item);
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.delete(createdItem.getId(), owner.getId() + 1));
    }

    /*
        Тесты List<Item> searchItems(Long userId, String text, Integer from, Integer size);
     */

    @Test
    public void testSearchItemsOutPage() {
        checkSearchItems(3, null, null);
    }

    @Test
    public void testSearchWithPageFirstElement() {
        checkSearchItems(3, 0, 5);
    }

    @Test
    public void testSearchWithOutPageEmptyList() {
        Assertions.assertTrue(itemService.searchItems(
                owner.getId(), "name", null, null).isEmpty());
    }

    @Test
    public void testSearchWithNullTestWithOutPageEmptyList() {
        Assertions.assertTrue(itemService.searchItems(
                owner.getId(), null, null, null).isEmpty());
    }

    @Test
    public void testSearchWithNullTestWithPageEmptyList() {
        Assertions.assertTrue(itemService.searchItems(
                owner.getId(), null, 0, 10).isEmpty());
    }

    @Test
    public void testSearchWithPageEmptyList() {
        Assertions.assertTrue(itemService.searchItems(
                owner.getId(), "name", 0, 10).isEmpty());
    }

    @Test
    public void testSearchWithPageMiddleElement() {
        checkSearchItems(2, 1, 5);
    }

    @Test
    public void testSearchWithPageTwoPage() {
        checkSearchItems(2, 1, 2);
    }

    @Test
    public void testSearchWithPageLastElement() {
        checkSearchItems(1, 2, 5);
    }

    @Test
    public void testSearchNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.searchItems(owner.getId() + 1,
                        "name", null, null));
    }

    @Test
    public void testSearchNoParamFrom() {
        Assertions.assertThrows(ValidationException.class,
                () -> itemService.searchItems(owner.getId(),
                        "name", null, 1));
    }

    @Test
    public void testSearchNoParamSize() {
        Assertions.assertThrows(ValidationException.class,
                () -> itemService.searchItems(owner.getId(), "name", 1, null));
    }

    private void checkSearchItems(int expSize, Integer from, Integer size) {
        User user = addUser();
        Item itemSave = itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());
        itemService.create(owner.getId(), null, getDefaultItem());

        Assertions.assertEquals(expSize, itemService.searchItems(
                user.getId(), itemSave.getName(), from, size).size());
    }

    /*
        Тесты на метод addComment(Long itemId, Long userId, Comment comment)
     */

    @Test
    public void testAddComment() {
        User requester = addUser();
        User booker = addUser();
        ItemRequest itemRequest = addItemRequest(requester);
        Item itemCreate = itemService.create(owner.getId(), itemRequest.getId(), item);
        Booking lastBooking = bookingRepository.save(
                getDefaultBooking(itemCreate, booker, LocalDateTime.now().minusDays(1)));

        Comment newComment = getNewComment();

        Comment comment =
                itemService.addComment(itemCreate.getId(), booker.getId(), newComment);

        Assertions.assertNotNull(comment.getId());
        Assertions.assertEquals(newComment.getText(), comment.getText());
        Assertions.assertTrue(newComment.getCreated().isEqual(comment.getCreated()));
        Assertions.assertEquals(userRepository.findById(booker.getId()).get(),
                comment.getAuthor());
        Assertions.assertEquals(itemCreate.getId(), comment.getItem().getId());
    }

    @Test
    public void testAddCommentFailedNoUser() {
        User booker = addUser();
        Item itemCreate = itemService.create(owner.getId(), null, item);
        Booking lastBooking = bookingRepository.save(
                getDefaultBooking(itemCreate, booker, LocalDateTime.now().minusDays(1)));

        Comment newComment = getNewComment();
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addComment(itemCreate.getId(),
                        booker.getId() + 1001, newComment));
    }

    @Test
    public void testAddCommentFailedNoItem() {
        User booker = addUser();

        Comment newComment = getNewComment();
        Assertions.assertThrows(NotFoundException.class,
                () -> itemService.addComment(1L,
                        booker.getId(), newComment));
    }

    @Test
    public void testAddCommentFailedNoBooking() {
        User booker = addUser();
        Item itemCreate = itemService.create(owner.getId(), null, item);

        Comment newComment = getNewComment();
        Assertions.assertThrows(ValidationException.class,
                () -> itemService.addComment(itemCreate.getId(),
                        booker.getId(), newComment));
    }

    private User addUser() {
        return userRepository.save(getDefaultUser(TestUtil.getRandomPartForEmail()));
    }

    private ItemRequest addItemRequest(User requester) {
        return itemRequestRepository.save(getDefaultRequest(requester));
    }

    private User getDefaultUser(String random) {
        return User.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private ItemRequest getDefaultRequest(User requester) {
        return ItemRequest.builder()
                .description("Новый запрос на вещь.")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }

    private Item getDefaultItem() {
        return Item.builder()
                .available(true)
                .name("New item")
                .description("New item description")
                .build();
    }

    private Item getDefaultUpdateItem() {
        return Item.builder()
                .available(true)
                .name("Update item")
                .description("Super update item")
                .build();
    }

    private Booking getDefaultBooking(Item item, User booker, LocalDateTime start) {
        return Booking.builder()
                .start(start)
                .end(start.plusDays(10))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
    }

    private Comment getNewComment() {
        return Comment.builder()
                .text("New comment")
                .created(LocalDateTime.now())
                .build();
    }

}
