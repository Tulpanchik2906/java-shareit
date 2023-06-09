package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    BookingRepository bookingRepository;

    private Long ownerId;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    public void beforeEach() throws Exception {
        ownerId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
    }

    @Test
    public void testCreateItemSuccess() throws Exception {
        Assertions.assertNotNull(sendRequestAddItem(getAllFieldsItem(), ownerId));
    }

    @Test
    public void testCreateItemFailedNoUserId() throws Exception {
        sendRequestAddItemWithFail(getAllFieldsItem(), getNoExistUserId());
    }

    @Test
    public void testGetItemListSuccess() throws Exception {
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        Assertions.assertTrue(sendRequestFindAllItemsByUserId(
                ownerId, null, null).size() > 0);
    }

    @Test
    public void testGetItemListSuccessWithFrom1Size2() throws Exception {
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        Assertions.assertNotNull(sendRequestFindAllItemsByUserId(ownerId, 1, 2));
    }

    @Test
    public void testGetItemListSuccessWithFrom0Size2() throws Exception {
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        Assertions.assertNotNull(sendRequestFindAllItemsByUserId(ownerId, 0, 2));
    }

    @Test
    public void testGetItemListSuccessWithFrom10Size2() throws Exception {
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        Assertions.assertNotNull(sendRequestFindAllItemsByUserId(ownerId, 10, 2));
    }

    @Test
    public void testGetItemListSuccessWithFrom9Size2() throws Exception {
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        Assertions.assertNotNull(sendRequestFindAllItemsByUserId(ownerId, 9, 2));
    }

    @Test
    public void testGetItemListFailedNoExistUser() throws Exception {
        sendRequestFindAllItemsByUserIdFailed(getNoExistUserId(), 1, 2);
    }

    @Test
    public void testGetItemListFailedNoFrom() throws Exception {
        sendRequestFindAllItemsByUserIdFailed(ownerId, null, 1);
    }

    @Test
    public void testGetItemListFailedNoSize() throws Exception {
        sendRequestFindAllItemsByUserIdFailed(ownerId, 0, null);
    }

    @Test
    public void testGetItemByIdSuccess() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        Assertions.assertNotNull(sendRequestGetItemById(itemDto.getId(), ownerId));
    }

    @Test
    public void testGetItemByIdWithBookingSuccess() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        UserDto userDto = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail()));

        User user = User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();

        Booking booking = Booking.builder()
                .booker(user)
                .build();

        when(bookingRepository
                .findByItemIdAndItemOwnerIdAndStatusAndEndBeforeOrderByEndDesc(
                        any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(booking)));

        when(bookingRepository
                .findByItemIdAndItemOwnerIdAndStatusAndStartBeforeAndEndAfterOrderByEndDesc(
                        any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(booking)));

        when(bookingRepository
                .findByItemIdAndItemOwnerIdAndStatusAndStartAfterOrderByStartAsc(
                        any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(booking)));

        Assertions.assertNotNull(sendRequestGetItemById(itemDto.getId(), ownerId));
    }


    @Test
    public void testGetItemByIdFailedNoExistUser() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestGetItemByIdFailed(itemDto.getId(), getNoExistUserId());
    }

    @Test
    public void testGetItemByIdFailedNoItemId() throws Exception {
        sendRequestGetItemByIdFailed(Long.valueOf(-1L), ownerId);
    }

    @Test
    public void testGetSearchSuccess() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        Assertions.assertTrue(sendRequestSearchItems(
                ownerId, null, null, itemDto.getName()).size() > 0);
    }

    @Test
    public void testGetSearchSuccessWithParam1And20() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);

        Assertions.assertTrue(sendRequestSearchItems(
                ownerId, 1, 20, itemDto.getName()).size() > 0);
    }

    @Test
    public void testGetSearchSuccessWithParam1And2() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);

        Assertions.assertTrue(sendRequestSearchItems(
                ownerId, 1, 2, itemDto.getName()).size() > 0);
    }

    @Test
    public void testGetSearchSuccessWithParam3And2() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);

        Assertions.assertTrue(sendRequestSearchItems(
                ownerId, 3, 2, itemDto.getName()).size() > 0);
    }

    @Test
    public void testGetSearchSuccessWithParam2And2() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);

        Assertions.assertTrue(sendRequestSearchItems(
                ownerId, 2, 2, itemDto.getName()).size() > 0);
    }

    @Test
    public void testGetSearchSuccessWithParam3And1() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);
        sendRequestAddItem(getAllFieldsItem(), ownerId);

        Assertions.assertTrue(sendRequestSearchItems(
                ownerId, 3, 1, itemDto.getName()).size() > 0);
    }


    @Test
    public void testGetSearchFailedNoUser() throws Exception {
        sendRequestSearchItemsFailed(
                getNoExistUserId(), null, null, "test text");
    }

    @Test
    public void testGetSearchFailedNoParam() throws Exception {
        sendRequestSearchItemsFailed(
                getNoExistUserId(), 1, null, "test text");
    }

    @Test
    public void testCreateCommentSuccess() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);
        UserDto booker = sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));

        Booking booking = Booking.builder().build();

        when(bookingRepository.findByBookerIdAndItemIdAndStatusAndStartBefore(
                any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(booking)));

        Assertions.assertNotNull(sendRequestAddComment(getNewCommentDto(),
                booker.getId(), itemDto.getId()));

    }

    @Test
    public void testCreateCommentFailedCommentBooker() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);

        sendRequestAddCommentFailed(getNewCommentDto(), ownerId, itemDto.getId());
    }

    @Test
    public void testUpdateItemSuccess() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);

        Assertions.assertNotNull(
                sendRequestUpdateItem(getAllFieldsForUpdateItem(), ownerId, itemDto.getId()));
    }

    @Test
    public void testUpdateItemFailedNoExistUser() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);

        sendRequestUpdateItemFailed(getAllFieldsForUpdateItem(), getNoExistUserId(), itemDto.getId());
    }

    @Test
    public void testDeleteItemSuccess() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);

        sendRequestDeleteItem(itemDto.getId(), ownerId);

    }

    @Test
    public void testDeleteItemFailedNoExistUser() throws Exception {
        ItemDto itemDto = sendRequestAddItem(getAllFieldsItem(), ownerId);

        sendRequestDeleteItemFailed(itemDto.getId(), getNoExistUserId());
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
                // .andExpect(status().isOk())
                .andReturn();
    }

    private void sendRequestAddItemWithFail(CreateItemDto createItemDto, long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(post("/items")
                        .headers(headers)
                        .content(objectMapper.writeValueAsString(createItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestFindAllItemsByUserIdFailed(
            long userId, Integer from, Integer size) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/items")
                        .headers(headers)
                        .params(TestUtil.getPageParams(from, size)))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestGetItemByIdFailed(long itemId, long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/items/" + itemId)
                        .headers(headers))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestSearchItemsFailed(
            long userId, Integer from, Integer size, String text) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));
        MultiValueMap<String, String> parameters = TestUtil.getPageParams(from, size);
        parameters.put("text", Collections.singletonList(text));


        MvcResult res = mockMvc.perform(get("/items/search")
                        .headers(headers)
                        .params(parameters))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestAddCommentFailed(
            CreateCommentDto createCommentDto, long userId, long itemId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(post("/items/" + itemId + "/comment")
                        .headers(headers)
                        .content(objectMapper.writeValueAsString(createCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestUpdateItemFailed(PatchItemDto patchItemDto, long userId, long itemId)
            throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(patch("/items/" + itemId)
                        .headers(headers)
                        .content(objectMapper.writeValueAsString(patchItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestDeleteItemFailed(long itemId, long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(delete("/items/" + itemId)
                        .headers(headers))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }


    private UserDto sendRequestAddUser(CreateUserDto user) throws Exception {
        MvcResult res = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        UserDto.class);
    }

    private void sendRequestDeleteUser(Long userId) throws Exception {
        MvcResult res = mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isOk())
                .andReturn();
    }

    private CreateUserDto getAllFieldsUser(String random) {
        return CreateUserDto.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private CreateItemDto getAllFieldsItem() {
        return CreateItemDto.builder()
                .name("New item")
                .description("Super new item")
                .available(true)
                .build();
    }

    private PatchItemDto getAllFieldsForUpdateItem() {
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

    private Long getNoExistUserId() throws Exception {
        UserDto userDto = sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));
        sendRequestDeleteUser(userDto.getId());
        return userDto.getId();
    }

}