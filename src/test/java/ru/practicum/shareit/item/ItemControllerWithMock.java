package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemControllerWithMock {
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ItemServiceImpl itemService;

    private ItemController itemController;

    private User user;
    private ItemRequest itemRequest;
    private Item item;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        itemService = mock(ItemServiceImpl.class);

        itemController = new ItemController(itemService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        user = getDefaultUser(TestUtil.getRandomPartForEmail(), 5L);
        itemRequest = getDefaultRequest(8L);
        item = getDefaultItem();
    }

    @Test
    public void testCreateItemSuccess() throws Exception {
        when(itemService.create(any(), any(), any()))
                .thenReturn(item);

        ItemDto res = sendRequestAddItem(getDefaultCreateItemDto(), user.getId());

        Assertions.assertNotNull(res);
    }

    @Test
    public void testUpdateItemSuccess() throws Exception {
        when(itemService.update(any(), any(), any(), any()))
                .thenReturn(item);

        ItemDto res = sendRequestUpdateItem(
                getDefaultUpdateItem(), user.getId(), item.getId());

        Assertions.assertNotNull(res);
    }

    @Test
    public void testGetItemSuccess() throws Exception {
        item = addBookingsAndComments(item);

        when(itemService.getInfo(any(), any()))
                .thenReturn(item);

        ItemDto res = sendRequestGetItemById(item.getId(), user.getId());

        Assertions.assertNotNull(res);
    }

    @Test
    public void testFindAllSuccess() throws Exception {
        item = addBookingsAndComments(item);

        when(itemService.findAllByUser(any(), any(), any()))
                .thenReturn(List.of(item, item, item));

        List<ItemDto> res = sendRequestFindAllItemsByUserId(
                user.getId(), 0, 3);

        Assertions.assertNotNull(res);
        Assertions.assertEquals(3, res.size());
    }

    @Test
    public void testSearchSuccess() throws Exception {
        item = addBookingsAndComments(item);

        when(itemService.searchItems(any(), any(), any(), any()))
                .thenReturn(List.of(item, item, item));

        List<ItemDto> res = sendRequestSearchItems(
                user.getId(), 0, 3, "поиск вещи");

        Assertions.assertNotNull(res);
        Assertions.assertEquals(3, res.size());
    }

    @Test
    public void testAddCommentSuccess() throws Exception {
        Comment comment = getDefaultComment(9L, user, getDefaultItem());

        when(itemService.addComment(any(), any(), any()))
                .thenReturn(comment);

        CommentDto res = sendRequestAddComment(getNewCommentDto(), user.getId(), item.getId());

        Assertions.assertNotNull(res);
    }

    @Test
    public void testDeleteItemSuccess() throws Exception {
        doNothing().when(itemService).delete(any(), any());
        sendRequestDeleteItem(item.getId(), user.getId());

        verify(itemService, times(1))
                .delete(any(Long.class), any(Long.class));
    }

    private ItemDto sendRequestAddItem(CreateItemDto createItemDto, long userId)
            throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(post("/items")
                        .headers(headers)
                        .content(objectMapper.writeValueAsString(createItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        ItemDto.class);
    }


    private List<ItemDto> sendRequestFindAllItemsByUserId(
            long userId, Integer from, Integer size) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/items")
                        .headers(headers)
                        .params(TestUtil.getPageParams(from, size)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(),
                ArrayList.class);
    }

    private List<ItemDto> sendRequestSearchItems(
            long userId, Integer from, Integer size, String text) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));
        MultiValueMap<String, String> parameters = TestUtil.getPageParams(from, size);
        parameters.put("text", Collections.singletonList(text));


        MvcResult res = mockMvc.perform(get("/items/search")
                        .headers(headers)
                        .params(parameters))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(),
                ArrayList.class);
    }


    private ItemDto sendRequestGetItemById(long itemId, long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/items/" + itemId)
                        .headers(headers))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(),
                ItemDto.class);
    }

    private CommentDto sendRequestAddComment(
            CreateCommentDto createCommentDto, long userId, long itemId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(post("/items/" + itemId + "/comment")
                        .headers(headers)
                        .content(objectMapper.writeValueAsString(createCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        CommentDto.class);
    }

    private ItemDto sendRequestUpdateItem(PatchItemDto patchItemDto, long userId, long itemId)
            throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(patch("/items/" + itemId)
                        .headers(headers)
                        .content(objectMapper.writeValueAsString(patchItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        ItemDto.class);
    }

    private void sendRequestDeleteItem(long itemId, long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(delete("/items/" + itemId)
                        .headers(headers))
                .andExpect(status().isOk())
                .andReturn();
    }

    private CreateItemDto getDefaultCreateItemDto() {
        return CreateItemDto.builder()
                .name("New item")
                .description("Super new item")
                .available(true)
                .build();
    }

    private PatchItemDto getDefaultUpdateItem() {
        return PatchItemDto.builder()
                .name("Update item")
                .description("Super update item")
                .build();
    }

    private CreateCommentDto getNewCommentDto() {
        return CreateCommentDto.builder()
                .text("New comment")
                .build();
    }

    private User getDefaultUser(String random, Long id) {
        return User.builder()
                .id(id)
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private Booking getDefaultBooking(User user, Long id) {
        return Booking.builder()
                .id(id)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();
    }

    private Comment getDefaultComment(Long id, User author, Item item) {
        return Comment.builder()
                .id(id)
                .text("Новый комментарий")
                .item(item)
                .author(author)
                .build();
    }

    private ItemRequest getDefaultRequest(Long id) {
        return ItemRequest.builder()
                .id(id)
                .description("Новый запрос на вещь.")
                .build();
    }

    private Item getDefaultItem() {
        CreateItemDto createItemDto = getDefaultCreateItemDto();
        return Item.builder()
                .id(1L)
                .owner(user)
                .available(createItemDto.getAvailable())
                .name(createItemDto.getName())
                .description(createItemDto.getDescription())
                .comments(Collections.EMPTY_LIST)
                .request(itemRequest)
                .build();
    }

    private Item addBookingsAndComments(Item item) {
        Booking lastBooking = getDefaultBooking(user, 6L);
        Booking nextBooking = getDefaultBooking(user, 7L);
        item.setLastBooking(lastBooking);
        item.setNextBooking(nextBooking);

        List<Comment> comments = List.of(
                getDefaultComment(9L, user, item),
                getDefaultComment(10L, user, item),
                getDefaultComment(10L, user, item));

        item.setComments(comments);
        return item;
    }
}
