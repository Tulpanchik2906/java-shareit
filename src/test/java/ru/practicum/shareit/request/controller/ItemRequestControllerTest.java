package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Long ownerId;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    public void beforeEach() throws Exception {
        ownerId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
    }


    @Test
    public void testCreateRequestSuccess() throws Exception {
        Assertions.assertNotNull(
                sendRequestAddRequest(getDefaultRequest(), ownerId));
    }

    @Test
    public void testCreateRequestFailedNoUser() throws Exception {
        sendRequestAddRequestFailed(getDefaultRequest(), getNoExistUserId());
    }

    @Test
    public void testGetRequestSuccess() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        ItemRequestDto itemRequestDto =
                sendRequestAddRequest(getDefaultRequest(), ownerId);
        Assertions.assertNotNull(sendRequestGetRequest(
                itemRequestDto.getId(), userId));

    }

    @Test
    public void testGetRequestFailedNoExistUser() throws Exception {
        Long userId = getNoExistUserId();
        ItemRequestDto itemRequestDto =
                sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestGetRequestFailed(itemRequestDto.getId(), userId);
    }

    @Test
    public void testGetFindAllRequestSuccess() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        Assertions.assertTrue(
                sendRequestFindAllRequest(ownerId).size() > 0);
    }

    @Test
    public void testGetFindAllRequestSuccessAnotherUser() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        Assertions.assertTrue(
                sendRequestFindAllRequest(userId).isEmpty());
    }

    @Test
    public void testGetFindAllRequestFailedNoUser() throws Exception {
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestFindAllRequestFailed(getNoExistUserId());
    }

    @Test
    public void testGetFindAllWithParamRequestSuccessWithOutParams() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        Assertions.assertTrue(
                sendRequestFindAllWithParamRequest(
                        userId, null, null).size() > 0);
    }

    @Test
    public void testGetFindAllRequestSuccess1PageFromLessSize() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        Assertions.assertTrue(
                sendRequestFindAllWithParamRequest(
                        userId, 0, 20).size() > 0);
    }

    @Test
    public void testGetFindAllRequestSuccess2PageFromLessSize() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        Assertions.assertTrue(
                sendRequestFindAllWithParamRequest(
                        userId, 2, 20).size() > 0);
    }

    @Test
    public void testGetFindAllRequestSuccess2PageFromMoreSize() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        Assertions.assertTrue(
                sendRequestFindAllWithParamRequest(
                        userId, 3, 2).size() > 0);
    }

    @Test
    public void testGetFindAllRequestSuccess1PageFromMoreSize() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        Assertions.assertTrue(
                sendRequestFindAllWithParamRequest(
                        userId, 2, 2).size() > 0);
    }

    @Test
    public void testGetFindAllWithParamRequestFailedOneParam() throws Exception {
        Long userId = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail())).getId();
        sendRequestAddRequest(getDefaultRequest(), ownerId);
        sendRequestFindAllWithParamRequestFailed(userId, null, 10);
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

    private List<ItemRequestDto> sendRequestFindAllWithParamRequest(
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

    private void sendRequestAddRequestFailed(
            CreateItemRequestDto req, Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestGetRequestFailed(
            Long reqId, Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/requests/" + reqId)
                        .headers(headers))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestFindAllRequestFailed(Long userId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/requests")
                        .headers(headers))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestFindAllWithParamRequestFailed(
            Long userId, Integer from, Integer size) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(X_SHARER_USER_ID, String.valueOf(userId));

        MvcResult res = mockMvc.perform(get("/requests/all")
                        .params(TestUtil.getPageParams(from, size))
                        .headers(headers))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private CreateItemRequestDto getDefaultRequest() {
        return CreateItemRequestDto.builder()
                .description("Новый запрос на вещь.")
                .build();
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

    private Long getNoExistUserId() throws Exception {
        UserDto userDto = sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));
        sendRequestDeleteUser(userDto.getId());
        return userDto.getId();
    }
}
