package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotAvailableItemException;
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase
public class BookingServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private Item item;
    private User booker;

    @BeforeEach
    public void beforeEach() {
        owner = addUser();
        booker = addUser();
        item = addItem(owner);
    }

    @AfterEach
    public void afterEach() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    /*
        Тесты на  Booking create(Booking booking, Long userId, Long itemId);
     */

    @Test
    public void testCreateBookingSuccess() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Booking res = bookingService.create(booking, booker.getId(), item.getId());

        checkBookingAfterCreate(booking, res);
    }

    @Test
    public void testCreateBookingFailedNoUser() {
        Booking booking = getDefaultBooking(LocalDateTime.now());

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.create(booking, booker.getId() + 10,
                        item.getId()));
    }

    @Test
    public void testCreateBookingFailedTime() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().minusDays(10));

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.create(booking, booker.getId(),
                        item.getId()));
    }

    @Test
    public void testCreateBookingNoAvailable() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Item item = getDefaultItem(owner);
        item.setAvailable(false);
        Item itemSave = itemRepository.save(item);

        Assertions.assertThrows(NotAvailableItemException.class,
                () -> bookingService.create(booking, booker.getId(),
                        itemSave.getId()));
    }

    @Test
    public void testCreateBookingNoItem() {
        Booking booking = getDefaultBooking(LocalDateTime.now());

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.create(booking, booker.getId(),
                        item.getId() + 1));
    }

    @Test
    public void testCreateBookingFailedBookingByOwner() {
        Booking booking = getDefaultBooking(LocalDateTime.now());

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.create(booking, owner.getId(),
                        item.getId()));
    }

    /*
        Тесты на метод Booking get(Long bookingId, Long userId);
     */
    @Test
    public void testGetBookingSuccessByBooker() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Booking saveBooking = bookingService.create(
                booking, booker.getId(), item.getId());

        Booking res = bookingService.get(saveBooking.getId(), booker.getId());

        checkBookingAfterCreate(booking, res);
    }

    @Test
    public void testGetBookingSuccessByOwner() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Booking saveBooking = bookingService.create(
                booking, booker.getId(), item.getId());

        Booking res = bookingService.get(saveBooking.getId(), owner.getId());

        checkBookingAfterCreate(booking, res);
    }

    @Test
    public void testGetBookingFailedByNewUser() {
        User visitor = addUser();
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Booking saveBooking = bookingService.create(
                booking, booker.getId(), item.getId());

        bookingService.get(saveBooking.getId(), owner.getId());

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.get(booking.getId(), visitor.getId()));
    }

    @Test
    public void testGetBookingFailedNoUser() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        bookingService.create(booking, booker.getId(), item.getId());

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.get(booking.getId(), owner.getId() + 3));
    }

    @Test
    public void testGetBookingFailedNoBookingId() {
        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.get(1L, owner.getId()));
    }

    /*
        Тесты на метод:
        Booking approve(Long bookingId, Long userId, boolean approved);
     */

    @Test
    public void testApproveSuccess() {
        testApprove(true, BookingStatus.APPROVED);
    }

    @Test
    public void testRejectSuccess() {
        testApprove(false, BookingStatus.REJECTED);
    }

    @Test
    public void testApproveFailedNoUser() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Booking saveBooking = bookingService.create(
                booking, booker.getId(), item.getId());

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.approve(saveBooking.getId(),
                        owner.getId() + 1, true));
    }

    @Test
    public void testApproveFailedNoBooking() {
        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.approve(1L,
                        owner.getId(), true));
    }

    @Test
    public void testApproveFailedDoubleApprove() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Booking saveBooking = bookingService.create(
                booking, booker.getId(), item.getId());

        bookingService.approve(saveBooking.getId(), owner.getId(), true);

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.approve(saveBooking.getId(),
                        owner.getId(), true));
    }

    @Test
    public void testApproveFailedBookerApprove() {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Booking saveBooking = bookingService.create(
                booking, booker.getId(), item.getId());

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.approve(saveBooking.getId(),
                        booker.getId(), true));
    }

    /*
           Тесты на метод:
           List<Booking> findAllByBooker(
                Long userId, String bookingState, Integer from, Integer size);
     */

    @Test
    public void testFindAllByBookerWithOutPageStateAll() {
        testFindAllByBookerStateAll(null, null, 2);
    }

    @Test
    public void testFindAllByBooker1PageStateAll() {
        testFindAllByBookerStateAll(0, 6, 2);
    }

    @Test
    public void testFindAllByBooker2PageStateAll() {
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        testFindAllByBookerStateAll(1, 2, 2);
    }

    @Test
    public void testFindAllByBookerWithOutPageStateCurrent() {
        testFindAllByBookerStateCurrent(null, null, 2);
    }

    @Test
    public void testFindAllByBooker1PageStateCurrent() {
        testFindAllByBookerStateCurrent(0, 6, 2);
    }

    @Test
    public void testFindAllByBooker2PageStateCurrent() {
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(1)),
                booker.getId(), item.getId());
        testFindAllByBookerStateCurrent(1, 2, 2);
    }

    @Test
    public void testFindAllByBookerWithOutPageStatePast() {
        testFindAllByBookerStatePast(null, null, 2);
    }

    @Test
    public void testFindAllByBooker1PageStatePast() {
        testFindAllByBookerStatePast(0, 6, 2);
    }

    @Test
    public void testFindAllByBooker2PageStatePast() {
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item.getId());
        testFindAllByBookerStatePast(1, 2, 2);
    }

    @Test
    public void testFindAllByBookerWithOutPageStateFuture() {
        testFindAllByBookerStateFuture(null, null, 2);
    }

    @Test
    public void testFindAllByBooker1PageStateFuture() {
        testFindAllByBookerStateFuture(0, 6, 2);
    }

    @Test
    public void testFindAllByBooker2PageStateFuture() {
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(10)),
                booker.getId(), item.getId());
        testFindAllByBookerStateFuture(1, 2, 2);
    }

    @Test
    public void testFindAllByBookerWithOutPageStateWaiting() {
        testFindAllByBookerStateWaiting(null, null, 2);
    }

    @Test
    public void testFindAllByBooker1PageStateWaiting() {
        testFindAllByBookerStateWaiting(0, 6, 2);
    }

    @Test
    public void testFindAllByBooker2PageStateWaiting() {
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        testFindAllByBookerStateWaiting(1, 2, 2);
    }

    @Test
    public void testFindAllByBookerWithOutPageStateRejected() {
        testFindAllByBookerStateRejected(null, null, 2);
    }

    @Test
    public void testFindAllByBooker1PageStateRejected() {
        testFindAllByBookerStateRejected(0, 6, 2);
    }

    @Test
    public void testFindAllByBooker2PageStateRejected() {
        Booking booking = bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.approve(booking.getId(), owner.getId(), false);
        testFindAllByBookerStateRejected(1, 2, 2);
    }

    @Test
    public void testFindAllByBookerFailedNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.findAllByBooker(owner.getId() + 3,
                        BookingState.ALL.name(), null, null));
    }

    @Test
    public void testFindAllByBookerFailedNoState() {
        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.findAllByBooker(owner.getId(),
                        "NO_SUPPORTED_STATE", null, null));
    }

    @Test
    public void testFindAllByBookerFailedNoFrom() {
        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.findAllByBooker(booker.getId(),
                        BookingState.ALL.name(), null, 1));
    }

    @Test
    public void testFindAllByBookerFailedNoSize() {
        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.findAllByBooker(booker.getId(),
                        BookingState.ALL.name(), 0, null));
    }


    /*
           Тесты на метод:
           List<Booking> findAllByOwner(
               Long userId, String bookingState, Integer from, Integer size);
     */

    @Test
    public void testFindAllByOwnerWithOutPageStateAll() {
        testFindAllByOwnerStateAll(null, null, 2);
    }

    @Test
    public void testFindAllByOwner1PageStateAll() {
        testFindAllByOwnerStateAll(0, 6, 2);
    }

    @Test
    public void testFindAllByOwner2PageStateAll() {
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        testFindAllByOwnerStateAll(1, 2, 2);
    }

    @Test
    public void testFindAllByOwnerWithOutPageStateCurrent() {
        testFindAllByOwnerStateCurrent(null, null, 2);
    }

    @Test
    public void testFindAllByOwner1PageStateCurrent() {
        testFindAllByOwnerStateCurrent(0, 6, 2);
    }

    @Test
    public void testFindAllByOwner2PageStateCurrent() {
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(1)),
                booker.getId(), item.getId());
        testFindAllByOwnerStateCurrent(1, 2, 2);
    }

    @Test
    public void testFindAllByOwnerWithOutPageStatePast() {
        testFindAllByOwnerStatePast(null, null, 2);
    }

    @Test
    public void testFindAllByOwner1PageStatePast() {
        testFindAllByOwnerStatePast(0, 6, 2);
    }

    @Test
    public void testFindAllByOwner2PageStatePast() {
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item.getId());
        testFindAllByOwnerStatePast(1, 2, 2);
    }

    @Test
    public void testFindAllByOwnerWithOutPageStateFuture() {
        testFindAllByOwnerStateFuture(null, null, 2);
    }

    @Test
    public void testFindAllByOwner1PageStateFuture() {
        testFindAllByOwnerStateFuture(0, 6, 2);
    }

    @Test
    public void testFindAllByOwner2PageStateFuture() {
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(10)),
                booker.getId(), item.getId());
        testFindAllByOwnerStateFuture(1, 2, 2);
    }

    @Test
    public void testFindAllByOwnerWithOutPageStateWaiting() {
        testFindAllByOwnerStateWaiting(null, null, 2);
    }

    @Test
    public void testFindAllByOwner1PageStateWaiting() {
        testFindAllByOwnerStateWaiting(0, 6, 2);
    }

    @Test
    public void testFindAllByOwner2PageStateWaiting() {
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        testFindAllByOwnerStateWaiting(1, 2, 2);
    }

    @Test
    public void testFindAllByOwnerWithOutPageStateRejected() {
        testFindAllByOwnerStateRejected(null, null, 2);
    }

    @Test
    public void testFindAllByOwner1PageStateRejected() {
        testFindAllByOwnerStateRejected(0, 6, 2);
    }

    @Test
    public void testFindAllByOwner2PageStateRejected() {
        Booking booking = bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.approve(booking.getId(), owner.getId(), false);
        testFindAllByOwnerStateRejected(1, 2, 2);
    }

    @Test
    public void testFindAllByOwnerFailedNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.findAllByOwner(owner.getId() + 3,
                        BookingState.ALL.name(), null, null));
    }

    @Test
    public void testFindAllByOwnerFailedNoFrom() {
        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.findAllByOwner(booker.getId(),
                        BookingState.ALL.name(), null, 1));
    }

    @Test
    public void testFindAllByOwnerFailedNoSize() {
        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.findAllByOwner(booker.getId(),
                        BookingState.ALL.name(), 0, null));
    }

    @Test
    public void testFindAllByOwnerFailedNoState() {
        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.findAllByOwner(owner.getId(),
                        "NO_SUPPORTED_STATE", null, null));
    }

    private void testFindAllByBookerStateAll(Integer from, Integer size,
                                             int expSizeList) {
        User booker2 = addUser();
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker2.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByBooker(
                booker.getId(), BookingState.ALL.name(),
                from, size).size());
    }

    private void testFindAllByBookerStateCurrent(Integer from, Integer size,
                                                 int expSizeList) {
        User booker2 = addUser();
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(1)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(1)),
                booker2.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(1)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(1)),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByBooker(
                booker.getId(), BookingState.CURRENT.name(),
                from, size).size());
    }

    private void testFindAllByBookerStatePast(Integer from, Integer size,
                                              int expSizeList) {
        User booker2 = addUser();
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(1)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker2.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByBooker(
                booker.getId(), BookingState.PAST.name(),
                from, size).size());
    }

    private void testFindAllByBookerStateFuture(Integer from, Integer size,
                                                int expSizeList) {
        User booker2 = addUser();
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(2)),
                booker2.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(2)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(2)),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByBooker(
                booker.getId(), BookingState.FUTURE.name(),
                from, size).size());
    }

    private void testFindAllByBookerStateWaiting(Integer from, Integer size,
                                                 int expSizeList) {
        User booker2 = addUser();
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker2.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByBooker(
                booker.getId(), BookingState.WAITING.name(),
                from, size).size());
    }

    private void testFindAllByBookerStateRejected(Integer from, Integer size,
                                                  int expSizeList) {
        User booker2 = addUser();
        Booking booking1 = bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        Booking booking2 = bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker2.getId(), item.getId());
        Booking booking3 = bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());

        bookingService.approve(booking1.getId(), owner.getId(), false);
        bookingService.approve(booking2.getId(), owner.getId(), false);
        bookingService.approve(booking3.getId(), owner.getId(), false);

        Assertions.assertEquals(expSizeList, bookingService.findAllByBooker(
                booker.getId(), BookingState.REJECTED.name(),
                from, size).size());
    }

    private void testFindAllByOwnerStateAll(Integer from, Integer size,
                                            int expSizeList) {
        User owner2 = addUser();
        Item item2 = addItem(owner2);
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item2.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByOwner(
                owner.getId(), BookingState.ALL.name(),
                from, size).size());
    }

    private void testFindAllByOwnerStateCurrent(Integer from, Integer size,
                                                int expSizeList) {
        User owner2 = addUser();
        Item item2 = addItem(owner2);
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(1)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(1)),
                booker.getId(), item2.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(1)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(1)),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByOwner(
                owner.getId(), BookingState.CURRENT.name(),
                from, size).size());
    }

    private void testFindAllByOwnerStatePast(Integer from, Integer size,
                                             int expSizeList) {
        User owner2 = addUser();
        Item item2 = addItem(owner2);
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(1)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item2.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByOwner(
                owner.getId(), BookingState.PAST.name(),
                from, size).size());
    }

    private void testFindAllByOwnerStateFuture(Integer from, Integer size,
                                               int expSizeList) {
        User owner2 = addUser();
        Item item2 = addItem(owner2);

        bookingService.create(getDefaultBooking(LocalDateTime.now().minusDays(100)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(2)),
                booker.getId(), item2.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(2)),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now().plusDays(2)),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByOwner(
                owner.getId(), BookingState.FUTURE.name(),
                from, size).size());
    }

    private void testFindAllByOwnerStateWaiting(Integer from, Integer size,
                                                int expSizeList) {
        User owner2 = addUser();
        Item item2 = addItem(owner2);
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item2.getId());
        bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());

        Assertions.assertEquals(expSizeList, bookingService.findAllByOwner(
                owner.getId(), BookingState.WAITING.name(),
                from, size).size());
    }

    private void testFindAllByOwnerStateRejected(Integer from, Integer size,
                                                 int expSizeList) {
        User owner2 = addUser();
        Item item2 = addItem(owner2);
        Booking booking1 = bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());
        Booking booking2 = bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item2.getId());
        Booking booking3 = bookingService.create(getDefaultBooking(LocalDateTime.now()),
                booker.getId(), item.getId());

        bookingService.approve(booking1.getId(), owner.getId(), false);
        bookingService.approve(booking2.getId(), owner2.getId(), false);
        bookingService.approve(booking3.getId(), owner.getId(), false);

        Assertions.assertEquals(expSizeList, bookingService.findAllByOwner(
                owner.getId(), BookingState.REJECTED.name(),
                from, size).size());
    }

    private void testApprove(boolean approved, BookingStatus bookingStatus) {
        Booking booking = getDefaultBooking(LocalDateTime.now());
        Booking saveBooking = bookingService.create(
                booking, booker.getId(), item.getId());
        bookingService.approve(saveBooking.getId(), owner.getId(), approved);

        Booking res = bookingService.get(saveBooking.getId(), owner.getId());
        Assertions.assertEquals(bookingStatus, res.getStatus());
    }

    private void checkBookingAfterCreate(Booking booking, Booking res) {
        Assertions.assertNotNull(res.getId());
        Assertions.assertEquals(BookingStatus.WAITING, res.getStatus());
        Assertions.assertEquals(booking.getStart().getSecond(),
                res.getStart().getSecond());
        Assertions.assertEquals(booking.getEnd().getSecond(),
                res.getEnd().getSecond());
        Assertions.assertEquals(booking.getItem().getId(), res.getItem().getId());
        Assertions.assertEquals(booking.getBooker().getId(),
                res.getBooker().getId());

    }

    private User addUser() {
        return userRepository.save(getDefaultUser(TestUtil.getRandomPartForEmail()));
    }

    private Item addItem(User owner) {
        return itemRepository.save(getDefaultItem(owner));
    }

    private User getDefaultUser(String random) {
        return User.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private Item getDefaultItem(User owner) {
        return Item.builder()
                .available(true)
                .name("New item")
                .description("New item description")
                .owner(owner)
                .build();
    }

    private Booking getDefaultBooking(LocalDateTime start) {
        return Booking.builder()
                .start(start)
                .end(start.plusDays(10))
                .status(BookingStatus.APPROVED)
                .build();
    }

}
