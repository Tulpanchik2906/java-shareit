package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.PatchUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateUserSuccess() throws Exception {
        Assertions.assertNotNull(sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail())));
    }

    @Test
    public void testGetErrorCreateUserWithReplay() throws Exception {
        CreateUserDto user = getAllFieldsUser(TestUtil.getRandomPartForEmail());
        sendRequestAddUser(user);
        sendRequestAddFailedUser(user);
    }

    @Test
    public void testUpdateUserSuccess() throws Exception {
        UserDto userDto = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail()));
        UserDto updateUserDto = sendRequestUpdateUser(
                getAllFieldsForPatchUser(TestUtil.getRandomPartForEmail()),
                userDto.getId());
        Assertions.assertNotNull(updateUserDto);
    }

    @Test
    public void testUpdateUserFailDoubleEmail() throws Exception {
        UserDto userDto = sendRequestAddUser(getAllFieldsUser(
                TestUtil.getRandomPartForEmail()));
        sendRequestUpdateFailedUser(
                getAllFieldsForPatchUser(userDto.getEmail()),
                userDto.getId());
    }

    @Test
    public void testUpdateUserFailUserNotFound() throws Exception {
        sendRequestUpdateFailedUser(getAllFieldsForPatchUser(TestUtil.getRandomPartForEmail()),
                getNoExistUserId());
    }

    @Test
    public void testGetUserSuccess() throws Exception {
        UserDto userDto = sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));
        Assertions.assertNotNull(sendRequestGetExistUser(userDto.getId()));
    }

    @Test
    public void testGetUserFailedNoExist() throws Exception {
        sendRequestGetFailedUser(getNoExistUserId());
    }

    @Test
    public void testDeleteUserSuccess() throws Exception {
        UserDto userDto = sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));
        sendRequestDeleteUser(userDto.getId());
        sendRequestGetFailedUser(userDto.getId());
    }

    @Test
    public void testDeleteUserFailedNoExistUser() throws Exception {
        sendRequestDeleteFailedUser(getNoExistUserId());
    }

    @Test
    public void testListUserSuccessManyUsers() throws Exception {
        sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));
        sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));
        sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));

        Assertions.assertTrue(sendRequestFindAll().size() > 0);
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

    private UserDto sendRequestUpdateUser(PatchUserDto user, Long userId) throws Exception {
        MvcResult res = mockMvc.perform(patch("/users/" + userId)
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        UserDto.class);
    }

    private UserDto sendRequestGetExistUser(Long userId) throws Exception {
        MvcResult res = mockMvc.perform(get("/users/" + userId))
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

    private List<UserDto> sendRequestFindAll() throws Exception {
        MvcResult res = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(),
                ArrayList.class);
    }


    private void sendRequestAddFailedUser(CreateUserDto user) throws Exception {
        MvcResult res = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestUpdateFailedUser(PatchUserDto user, Long userId) throws Exception {
        MvcResult res = mockMvc.perform(patch("/users/" + userId)
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestGetFailedUser(Long userId) throws Exception {
        MvcResult res = mockMvc.perform(get("/users/" + userId))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private void sendRequestDeleteFailedUser(Long userId) throws Exception {
        MvcResult res = mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }


    private CreateUserDto getAllFieldsUser(String random) {
        return CreateUserDto.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private PatchUserDto getAllFieldsForPatchUser(String random) {
        return PatchUserDto.builder()
                .name("Update user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private Long getNoExistUserId() throws Exception {
        UserDto userDto = sendRequestAddUser(getAllFieldsUser(TestUtil.getRandomPartForEmail()));
        sendRequestDeleteUser(userDto.getId());
        return userDto.getId();
    }

}