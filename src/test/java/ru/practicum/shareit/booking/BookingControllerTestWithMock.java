package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTestWithMock {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private BookingServiceImpl bookingService;

    private BookingController bookingController;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingServiceImpl.class);

        bookingController = new BookingController(bookingService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        owner = getDefaultUser(TestUtil.getRandomPartForEmail(), 5L);
        booker = getDefaultUser(TestUtil.getRandomPartForEmail(), 7L);
        item = getDefaultItem(2L, owner);
    }

    @Test
    public void testCreateBookingSuccess() throws Exception {
        when(bookingService.create(any(), any(), any()))
                .thenReturn(getDefaultBooking(1L, booker, item));

        BookingDto bookingDto = sendRequestCreateBooking(
                getDefaultBookingCreateDto(item.getId()), booker.getId());

        Assertions.assertNotNull(bookingDto);
    }

    @Test
    public void testApproveSuccess() throws Exception {
        when(bookingService.approve(any(), any(), anyBoolean()))
                .thenReturn(getDefaultBooking(1L, booker, item));

        BookingDto bookingDto = sendRequestApproveBooking(1L,
                owner.getId(), true);

        Assertions.assertNotNull(bookingDto);
    }

    @Test
    public void testGetSuccess() throws Exception {
        when(bookingService.get(any(), any()))
                .thenReturn(getDefaultBooking(1L, booker, item));

        BookingDto bookingDto = sendRequestGetBooking(1L, owner.getId());

        Assertions.assertNotNull(bookingDto);
    }

    @Test
    public void testFindAllByBookerSuccess() throws Exception {
        when(bookingService.findAllByBooker(any(), any(), any(), any()))
                .thenReturn(List.of(getDefaultBooking(1L, booker, item),
                        getDefaultBooking(3L, booker, item)));

        List<BookingDto> bookings = sendRequestFindAllByBookerBookingWithParams(
                booker.getId(), "ALL", 1, 20);

        Assertions.assertNotNull(bookings);
    }

    @Test
    public void testFindAllByOwnerSuccess() throws Exception {
        when(bookingService.findAllByOwner(any(), any(), any(), any()))
                .thenReturn(List.of(getDefaultBooking(1L, booker, item),
                        getDefaultBooking(3L, booker, item)));

        List<BookingDto> bookings = sendRequestFindAllByOwnerWithParams(
                booker.getId(), "ALL", 1, 20);

        Assertions.assertNotNull(bookings);
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

    private Booking getDefaultBooking(Long id, User user, Item item) {
        return Booking.builder()
                .id(id)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .booker(user)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
    }

    private Item getDefaultItem(Long itemId, User user) {
        return Item.builder()
                .id(itemId)
                .owner(user)
                .available(true)
                .name("Item name")
                .description("Item description")
                .comments(Collections.EMPTY_LIST)
                .build();
    }

    private User getDefaultUser(String random, Long id) {
        return User.builder()
                .id(id)
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private BookingCreateDto getDefaultBookingCreateDto(Long itemId) {
        return BookingCreateDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemId)
                .build();
    }
}


