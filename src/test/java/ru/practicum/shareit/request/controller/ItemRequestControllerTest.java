package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemRequestControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ItemRequestServiceImpl itemRequestService;

    private ItemRequestController itemRequestServiceController;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private User requester;

    @BeforeEach
    void setUp() {
        itemRequestService = mock(ItemRequestServiceImpl.class);

        itemRequestServiceController = new ItemRequestController(itemRequestService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(itemRequestServiceController)
                .build();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        requester = getDefaultUser("lala", 5L);

    }

    @Test
    public void testCreateRequestSuccess() throws Exception {
        when(itemRequestService.create(any(), any()))
                .thenReturn(getDefaultRequest(4L, requester, Collections.EMPTY_LIST));

        ItemRequestDto itemRequestDto = sendRequestAddRequest(
                getDefaultCreateItemRequestDto(), requester.getId());

        Assertions.assertNotNull(itemRequestDto);
    }

    @Test
    public void testGetRequestSuccess() throws Exception {
        ItemRequest itemRequest = getDefaultRequest(4L, requester, Collections.EMPTY_LIST);
        User user = getDefaultUser("lala", 6L);
        Item item1 = getDefaultItem(9L, user);
        item1.setRequest(itemRequest);
        Item item2 = getDefaultItem(10L, user);
        item2.setRequest(itemRequest);

        List<Item> items = List.of(item1, item2);

        when(itemRequestService.get(any(), any()))
                .thenReturn(getDefaultRequest(4L, requester, items));

        ItemRequestDto itemRequestDto = sendRequestGetRequest(4L, requester.getId());

        Assertions.assertNotNull(itemRequestDto);
    }

    @Test
    public void testFindAllRequestSuccess() throws Exception {
        ItemRequest itemRequest1 = getDefaultRequest(4L, requester, Collections.EMPTY_LIST);
        ItemRequest itemRequest2 = getDefaultRequest(4L, requester, Collections.EMPTY_LIST);

        List<ItemRequest> mockRequests = List.of(itemRequest1, itemRequest2);


        when(itemRequestService.findAllByRequesterId(any()))
                .thenReturn(mockRequests);

        List<ItemRequestDto> itemRequests = sendRequestFindAllRequest(requester.getId());

        Assertions.assertEquals(2, itemRequests.size());
    }

    @Test
    public void testFindAllRequestWithParamsSuccess() throws Exception {
        ItemRequest itemRequest1 = getDefaultRequest(4L, requester, Collections.EMPTY_LIST);
        ItemRequest itemRequest2 = getDefaultRequest(4L, requester, Collections.EMPTY_LIST);

        List<ItemRequest> mockRequests = List.of(itemRequest1, itemRequest2);


        when(itemRequestService.findAllByOffset(any(), any(), any()))
                .thenReturn(mockRequests);

        List<ItemRequestDto> itemRequests = sendRequestFindAllWithParamsRequest(
                requester.getId(), 1, 2);

        Assertions.assertEquals(2, itemRequests.size());
    }

    private ItemRequestDto sendRequestAddRequest(
            CreateItemRequestDto req, Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        ItemRequestDto.class);
    }

    private ItemRequestDto sendRequestGetRequest(
            Long reqId, Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/requests/" + reqId)
                        .headers(headers))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        ItemRequestDto.class);
    }

    private List<ItemRequestDto> sendRequestFindAllRequest(Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/requests")
                        .headers(headers))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(),
                ArrayList.class);
    }

    private List<ItemRequestDto> sendRequestFindAllWithParamsRequest(
            Long userId, Integer from, Integer size) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/requests/all")
                        .params(TestUtil.getPageParams(from, size))
                        .headers(headers))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(),
                ArrayList.class);
    }

    private CreateItemRequestDto getDefaultCreateItemRequestDto() {
        return CreateItemRequestDto.builder()
                .description("Новый запрос на вещь.")
                .build();
    }

    private User getDefaultUser(String random, Long id) {
        return User.builder()
                .id(id)
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private ItemRequest getDefaultRequest(Long id, User requester, List<Item> items) {
        return ItemRequest.builder()
                .id(id)
                .description("Новый запрос на вещь.")
                .requester(requester)
                .items(items)
                .created(LocalDateTime.now())
                .build();
    }

    private Item getDefaultItem(Long itemId, User owner) {
        return Item.builder()
                .id(itemId)
                .owner(owner)
                .available(true)
                .name("Item name")
                .description("Item description")
                .comments(Collections.EMPTY_LIST)
                .build();
    }
}
