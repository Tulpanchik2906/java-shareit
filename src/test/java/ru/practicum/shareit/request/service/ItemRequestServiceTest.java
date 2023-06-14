package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase
public class ItemRequestServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requester;

    @BeforeEach
    public void beforeEach() {
        requester = addUser();
    }

    @AfterEach
    public void afterEach() {
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    /*
        Тесты на ItemRequest create(ItemRequest itemRequest, Long userId);
     */

    @Test
    public void testCreateItemRequestSuccess() {
        ItemRequest create = getDefaultCreateItemRequest();
        ItemRequest res = itemRequestService.create(create, requester.getId());

        Assertions.assertNotNull(res.getId());
        Assertions.assertEquals(create.getDescription(), res.getDescription());
        Assertions.assertNotNull(create.getCreated());
        Assertions.assertEquals(requester.getId(), res.getRequester().getId());
        Assertions.assertEquals(Collections.EMPTY_LIST, res.getItems());
    }

    @Test
    public void testCreateItemRequestFailedNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.create(getDefaultCreateItemRequest(),
                        requester.getId() + 1));
    }

    /*
         ItemRequest get(Long requestId, Long userId);
     */

    @Test
    public void testGetItemRequestSuccess() {
        ItemRequest createdItemRequest = itemRequestService
                .create(getDefaultCreateItemRequest(), requester.getId());
        ItemRequest res = itemRequestService.get(createdItemRequest.getId(), requester.getId());


        Assertions.assertNotNull(res.getId());
        Assertions.assertEquals(createdItemRequest.getDescription(), res.getDescription());
        Assertions.assertEquals(createdItemRequest.getCreated().getSecond(),
                res.getCreated().getSecond());
        Assertions.assertEquals(requester.getId(), res.getRequester().getId());
        Assertions.assertEquals(Collections.EMPTY_LIST, res.getItems());
    }

    @Test
    public void testGetItemRequestWithItemsSuccess() {
        ItemRequest createdItemRequest = itemRequestService
                .create(getDefaultCreateItemRequest(), requester.getId());

        User owner = addUser();
        addItem(owner, createdItemRequest);
        addItem(owner, createdItemRequest);

        ItemRequest res = itemRequestService.get(createdItemRequest.getId(), requester.getId());

        Assertions.assertNotNull(res.getId());
        Assertions.assertEquals(createdItemRequest.getDescription(), res.getDescription());
        Assertions.assertEquals(createdItemRequest.getCreated().getSecond(),
                res.getCreated().getSecond());
        Assertions.assertEquals(requester.getId(), res.getRequester().getId());
        Assertions.assertEquals(2, res.getItems().size());
    }

    @Test
    public void testGetItemRequestFailedNoUser() {
        ItemRequest createdItemRequest = itemRequestService
                .create(getDefaultCreateItemRequest(), requester.getId());
        Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.get(createdItemRequest.getId(),
                        requester.getId() + 1));
    }

    @Test
    public void testGetItemRequestFailedNoRequest() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.get(1L, requester.getId()));
    }

    /*
        Тесты на метод: List<ItemRequest> findAllByUserId(Long userId);
     */

    @Test
    public void testFindAllByUserIdSuccess() {
        User requester2 = addUser();
        ItemRequest createdItemRequest = itemRequestService
                .create(getDefaultCreateItemRequest(), requester.getId());
        itemRequestService.create(getDefaultCreateItemRequest(), requester.getId());
        itemRequestService.create(getDefaultCreateItemRequest(), requester.getId());
        itemRequestService.create(getDefaultCreateItemRequest(), requester2.getId());

        User owner = addUser();
        addItem(owner, createdItemRequest);
        addItem(owner, createdItemRequest);


        List<ItemRequest> all = itemRequestService.findAllByRequesterId(requester.getId());

        Assertions.assertEquals(3, all.size());

    }

    @Test
    public void testFindAllByRequesterIdFailedNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.findAllByRequesterId(
                        requester.getId() + 1));
    }

    /*
        Тесты на метод: List<ItemRequest> findAllByOffset(Long userId, Integer from, Integer size);
     */

    @Test
    public void testFindAllByOffsetIdWithOutPageSuccess() {
        testFindAllByOffsetId(3, null, null);
    }

    @Test
    public void testFindAllByOffsetIdWith1PageSuccess() {
        testFindAllByOffsetId(1, 2, 2);
    }

    @Test
    public void testFindAllByOffsetIdWith2PageSuccess() {
        testFindAllByOffsetId(2, 1, 2);
    }

    @Test
    public void testFindAllByOffsetIdFailedNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> itemRequestService.findAllByOffset(requester.getId() + 1,
                        null, null));
    }

    @Test
    public void testFindAllByOffsetIdFailedNoFrom() {
        Assertions.assertThrows(ValidationException.class,
                () -> itemRequestService.findAllByOffset(requester.getId(),
                        null, 10));
    }

    @Test
    public void testFindAllByOffsetIdFailedNoSize() {
        Assertions.assertThrows(ValidationException.class,
                () -> itemRequestService.findAllByOffset(requester.getId(),
                        0, null));
    }

    private void testFindAllByOffsetId(int sizeList, Integer from, Integer sizePage) {
        User requester2 = addUser();
        ItemRequest createdItemRequest = itemRequestService
                .create(getDefaultCreateItemRequest(), requester.getId());
        itemRequestService.create(getDefaultCreateItemRequest(), requester.getId());
        itemRequestService.create(getDefaultCreateItemRequest(), requester.getId());
        itemRequestService.create(getDefaultCreateItemRequest(), requester2.getId());

        User owner = addUser();
        addItem(owner, createdItemRequest);
        addItem(owner, createdItemRequest);


        List<ItemRequest> all = itemRequestService.findAllByOffset(requester2.getId(),
                from, sizePage);

        Assertions.assertEquals(sizeList, all.size());
    }

    private User addUser() {
        return userRepository.save(getDefaultUser(TestUtil.getRandomPartForEmail()));
    }

    private Item addItem(User owner, ItemRequest itemRequest) {
        return itemRepository.save(getDefaultItem(owner, itemRequest));
    }

    private User getDefaultUser(String random) {
        return User.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private Item getDefaultItem(User owner, ItemRequest itemRequest) {
        return Item.builder()
                .available(true)
                .name("New item")
                .description("New item description")
                .owner(owner)
                .request(itemRequest)
                .build();
    }

    private ItemRequest getDefaultCreateItemRequest() {
        return ItemRequest.builder()
                .description("Новый запрос на вещь.")
                .build();
    }
}
