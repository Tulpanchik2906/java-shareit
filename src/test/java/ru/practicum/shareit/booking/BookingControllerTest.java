package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @AfterEach
    @Rollback
    public void afterAll() throws Exception {
    }

    @Test
    public void testCreateBookingSuccess() throws Exception {
        Assertions.assertNotNull(createBooking(getAllFieldsBooking(itemId1), bookerId));
    }

    @Test
    public void testApproveTrueBookingSuccess() throws Exception {
        Long userId2 = addUser(getAllFieldsUser(TestUtil.getRandomPartForEmail())).getId();
        BookingDto bookingDto = createBooking(getAllFieldsBooking(itemId1), userId2);

        BookingDto bookingDtoRes = approveBooking(bookingDto.getId(), ownerId, true);

        Assertions.assertEquals(bookingDtoRes.getStatus(), BookingStatus.APPROVED);
    }

    @Test
    public void testApproveFalseBookingSuccess() throws Exception {
        BookingDto bookingDto = createBooking(getAllFieldsBooking(itemId1), bookerId);

        BookingDto bookingDtoRes = approveBooking(bookingDto.getId(), ownerId, false);

        Assertions.assertEquals(bookingDtoRes.getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void testGetBookingSuccess() throws Exception {
        BookingDto bookingDto = createBooking(getAllFieldsBooking(itemId1), bookerId);

        BookingDto bookingDtoRes = getBooking(bookingDto.getId(), ownerId);

        Assertions.assertNotNull(bookingDtoRes);
    }

    private BookingDto getBooking(Long bookingId, Long userId) throws Exception {
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


    private BookingDto approveBooking(Long bookingId, Long userId, boolean approved) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));
        String url = "/bookings/" + bookingId + "?approved=" + Boolean.valueOf(approved).toString();
        MvcResult res = mockMvc.perform(patch(url)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        BookingDto.class);
    }

    private BookingDto createBooking(BookingCreateDto bookingCreateDto,
                                     Long userId) throws Exception {
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