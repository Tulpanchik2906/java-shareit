package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Long ownerId;
    private Long itemId1;
    private Long bookerId;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    public void beforeEach() throws Exception {
        ownerId = addUser(getAllFieldsUser(TestUtil.getRandomPartForEmail())).getId();
        itemId1 = addItem(getAllFieldsItem(), ownerId).getId();
        bookerId = addUser(getAllFieldsUser(TestUtil.getRandomPartForEmail())).getId();
    }

    @Test
    public void testCreateBookingSuccess() throws Exception {
        Assertions.assertNotNull(sendRequestCreateBooking(getAllFieldsBooking(itemId1), bookerId));
    }

    @Test
    public void testCreateBookingFailedNoUser() throws Exception {
        sendRequestCreateBookingFailed(getAllFieldsBooking(itemId1), Long.valueOf(-1L));
    }

    @Test
    public void testCreateBookingFailedAvailableItem() throws Exception {
        CreateItemDto createItemDto = getAllFieldsItem();
        createItemDto.setAvailable(false);
        ItemDto itemId2 = addItem(createItemDto, ownerId);
        sendRequestCreateBookingFailed(getAllFieldsBooking(itemId2.getId()), bookerId);
    }

    @Test
    public void testApproveTrueBookingSuccess() throws Exception {
        BookingDto bookingDto = sendRequestCreateBooking(getAllFieldsBooking(itemId1), bookerId);

        BookingDto bookingDtoRes = sendRequestApproveBooking(
                bookingDto.getId(), ownerId, true);

        Assertions.assertEquals(bookingDtoRes.getStatus(), BookingStatus.APPROVED);
    }

    @Test
    public void testApproveFalseBookingSuccess() throws Exception {
        BookingDto bookingDto = sendRequestCreateBooking(getAllFieldsBooking(itemId1), bookerId);

        BookingDto bookingDtoRes = sendRequestApproveBooking(
                bookingDto.getId(), ownerId, false);

        Assertions.assertEquals(bookingDtoRes.getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void testApproveTrueBookingFailedApproveByBooker() throws Exception {
        BookingDto bookingDto = sendRequestCreateBooking(getAllFieldsBooking(itemId1), bookerId);

        sendRequestApproveBookingFailed(bookingDto.getId(), bookerId, true);
    }

    @Test
    public void testApproveTrueBookingFailedNoWaitingStatus() throws Exception {
        BookingDto bookingDto = sendRequestCreateBooking(getAllFieldsBooking(itemId1), bookerId);

        sendRequestApproveBooking(bookingDto.getId(), ownerId, true);

        sendRequestApproveBookingFailed(bookingDto.getId(), ownerId, true);
    }

    @Test
    public void testGetBookingSuccess() throws Exception {
        BookingDto bookingDto = sendRequestCreateBooking(getAllFieldsBooking(itemId1), bookerId);

        BookingDto bookingDtoRes = sendRequestGetBooking(bookingDto.getId(), ownerId);

        Assertions.assertNotNull(bookingDtoRes);
    }

    @Test
    public void testFindAllByBookerStateAll() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.ALL.name(), null, null));

    }

    @Test
    public void testFindAllByBookerStateCurrent() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.CURRENT.name(), null, null));
    }

    @Test
    public void testFindAllByBookerStatePast() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.PAST.name(), null, null));
    }

    @Test
    public void testFindAllByBookerStateFeature() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.FUTURE.name(), null, null));
    }

    @Test
    public void testFindAllByBookerStateWaiting() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.WAITING.name(), null, null));
    }

    @Test
    public void testFindAllByBookerStateRejected() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.REJECTED.name(), null, null));
    }

    @Test
    public void testFindAllByBookerStateAlWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.ALL.name(), 0, 20));
    }

    @Test
    public void testFindAllByBookerStateCurrentWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.CURRENT.name(), 0, 20));
    }

    @Test
    public void testFindAllByBookerStatePastWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.PAST.name(), 0, 20));
    }

    @Test
    public void testFindAllByBookerStateFeatureWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.FUTURE.name(), 0, 20));
    }

    @Test
    public void testFindAllByBookerStateWaitingWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.WAITING.name(), 0, 20));
    }

    @Test
    public void testFindAllByBookerStateRejectedWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByBookerBookingWithParams(bookerId,
                        BookingState.REJECTED.name(), 0, 20));
    }

    @Test
    public void testFindAllByBookerFailedNoStatus() throws Exception {
        sendRequestFindAllByBookerBookingWithParamsFailed(
                bookerId, "NO_STATUS", null, null);
    }

    @Test
    public void testFindAllByBookerWithParamFailedNoStatus() throws Exception {
        sendRequestFindAllByBookerBookingWithParamsFailed(bookerId,
                "NO_STATUS", 0, 20);
    }

    @Test
    public void testFindAllByBookerFailedNoParam() throws Exception {
        sendRequestFindAllByBookerBookingWithParamsFailed(
                bookerId, BookingState.ALL.name(), null, 1);
    }

    @Test
    public void testFindAllByOwnerStateAll() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.ALL.name(), null, null));

    }

    @Test
    public void testFindAllByOwnerStateCurrent() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.CURRENT.name(), null, null));
    }

    @Test
    public void testFindAllByOwnerStatePast() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.PAST.name(), null, null));
    }

    @Test
    public void testFindAllByOwnerStateFeature() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.FUTURE.name(), null, null));
    }

    @Test
    public void testFindAllByOwnerStateWaiting() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.WAITING.name(), null, null));
    }

    @Test
    public void testFindAllByOwnerStateRejected() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.REJECTED.name(), null, null));
    }

    @Test
    public void testFindAllByOwnerStateAlWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.ALL.name(), 0, 20));
    }

    @Test
    public void testFindAllByOwnerStateCurrentWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.CURRENT.name(), 0, 20));
    }

    @Test
    public void testFindAllByOwnerStatePastWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.PAST.name(), 0, 20));
    }

    @Test
    public void testFindAllByOwnerStateFeatureWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.FUTURE.name(), 0, 20));
    }

    @Test
    public void testFindAllByOwnerStateWaitingWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.WAITING.name(), 0, 20));
    }

    @Test
    public void testFindAllByOwnerStateRejectedWithParams() throws Exception {
        Assertions.assertNotNull(
                sendRequestFindAllByOwnerWithParams(ownerId,
                        BookingState.REJECTED.name(), 0, 20));
    }

    @Test
    public void testFindAllByOwnerFailedNoStatus() throws Exception {
        sendRequestFindAllByOwnerWithParamsFailed(
                ownerId, "NO_STATUS", null, null);
    }

    @Test
    public void testFindAllByOwnerWithParamFailedNoStatus() throws Exception {
        sendRequestFindAllByOwnerWithParamsFailed(
                ownerId, "NO_STATUS", 0, 20);
    }

    @Test
    public void testFindAllByOwnerFailedNoParam() throws Exception {
        sendRequestFindAllByOwnerWithParamsFailed(
                ownerId, BookingState.ALL.name(), null, 1);
    }


    private BookingDto sendRequestGetBooking(Long bookingId, Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));
        String url = "/bookings/" + bookingId;
        MvcResult res = mockMvc.perform(get(url)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        BookingDto.class);
    }


    private BookingDto sendRequestApproveBooking(
            Long bookingId, Long userId, boolean approved) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));
        String url = "/bookings/" + bookingId + "?approved="
                + Boolean.valueOf(approved).toString();
        MvcResult res = mockMvc.perform(patch(url)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        BookingDto.class);
    }

    private BookingDto sendRequestCreateBooking(
            BookingCreateDto bookingCreateDto, Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(post("/bookings")
                        .headers(headers)
                        .content(objectMapper.writeValueAsString(bookingCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        BookingDto.class);
    }

    private List<BookingDto> sendRequestFindAllByBookerBookingWithParams(
            Long userId, String state, Integer from, Integer size) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MultiValueMap params = TestUtil.getPageParams(from, size);
        params.put("state", Collections.singletonList(state));

        MvcResult res = mockMvc.perform(get("/bookings")
                        .headers(headers)
                        .params(params))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(res.getResponse().getContentAsString(),
                ArrayList.class);
    }

    private List<BookingDto> sendRequestFindAllByOwnerWithParams(
            Long userId, String state, Integer from, Integer size) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MultiValueMap params = TestUtil.getPageParams(from, size);
        params.put("state", Collections.singletonList(state));

        MvcResult res = mockMvc.perform(get("/bookings/owner")
                        .headers(headers)
                        .params(params))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(res.getResponse().getContentAsString(),
                ArrayList.class);
    }

    private void sendRequestCreateBookingFailed(BookingCreateDto bookingCreateDto,
                                                Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(post("/bookings")
                        .headers(headers)
                        .content(objectMapper.writeValueAsString(bookingCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestApproveBookingFailed(
            Long bookingId, Long userId, boolean approved) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));
        String url = "/bookings/" + bookingId + "?approved="
                + Boolean.valueOf(approved).toString();
        MvcResult res = mockMvc.perform(patch(url)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestFindAllByBookerBookingWithParamsFailed(
            Long userId, String state, Integer from, Integer size) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MultiValueMap params = TestUtil.getPageParams(from, size);
        params.put("state", Collections.singletonList(state));

        MvcResult res = mockMvc.perform(get("/bookings")
                        .headers(headers)
                        .params(params))
                .andExpect(status().is4xxClientError())
                .andReturn();

    }

    private void sendRequestFindAllByOwnerWithParamsFailed(
            Long userId, String state, Integer from, Integer size) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MultiValueMap params = TestUtil.getPageParams(from, size);
        params.put("state", Collections.singletonList(state));

        MvcResult res = mockMvc.perform(get("/bookings/owner")
                        .headers(headers)
                        .params(params))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private ItemDto addItem(CreateItemDto createItemDto, long userId) throws Exception {
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

    private UserDto addUser(CreateUserDto user) throws Exception {
        MvcResult res = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        UserDto.class);
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
                .available(Boolean.valueOf(Boolean.TRUE))
                .build();
    }

    private BookingCreateDto getAllFieldsBooking(Long itemId) {
        return BookingCreateDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();
    }
}