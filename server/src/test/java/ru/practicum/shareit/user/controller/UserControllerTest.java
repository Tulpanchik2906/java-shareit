package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.PatchUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UserServiceImpl userService;

    private UserController userController;

    private CreateUserDto createUserDto;


    @BeforeEach
    void setUp() {
        userService = mock(UserServiceImpl.class);

        userController = new UserController(userService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

        createUserDto = getDefaultCreateUserDto(TestUtil.getRandomPartForEmail());


    }

    @Test
    public void testCreateUserSuccess() throws Exception {
        when(userService.create(any()))
                .thenReturn(User.builder()
                        .id(1L)
                        .name(createUserDto.getName())
                        .email(createUserDto.getEmail())
                        .build());

        UserDto res = sendRequestAddUser(createUserDto);

        Assertions.assertEquals(1L, res.getId());
        Assertions.assertEquals(createUserDto.getName(), res.getName());
        Assertions.assertEquals(createUserDto.getEmail(), res.getEmail());
    }

    @Test
    public void testUpdateUserSuccess() throws Exception {
        PatchUserDto patchUserDto = getDefaultUpdateUser(TestUtil.getRandomPartForEmail());

        when(userService.update(any(), any()))
                .thenReturn(User.builder()
                        .id(1L)
                        .name(patchUserDto.getName())
                        .email(patchUserDto.getEmail())
                        .build());

        UserDto res = sendRequestUpdateUser(patchUserDto, 1L);

        Assertions.assertEquals(1L, res.getId());
        Assertions.assertEquals(patchUserDto.getName(), res.getName());
        Assertions.assertEquals(patchUserDto.getEmail(), res.getEmail());
    }

    @Test
    public void testGetUserSuccess() throws Exception {
        when(userService.get(any()))
                .thenReturn(User.builder()
                        .id(1L)
                        .name(createUserDto.getName())
                        .email(createUserDto.getEmail())
                        .build());

        UserDto res = sendRequestGetUser(1L);

        Assertions.assertEquals(1L, res.getId());
        Assertions.assertEquals(createUserDto.getName(), res.getName());
        Assertions.assertEquals(createUserDto.getEmail(), res.getEmail());
    }

    @Test
    public void testDeleteUserSuccess() throws Exception {
        doNothing().when(userService).delete(any());
        sendRequestDeleteUser(1L);

        verify(userService, times(1)).delete(any(Long.class));
    }

    @Test
    public void testFindAllSuccess() throws Exception {
        User user1 = getDefaultUser(TestUtil.getRandomPartForEmail(), 1L);
        User user2 = getDefaultUser(TestUtil.getRandomPartForEmail(), 2L);
        User user3 = getDefaultUser(TestUtil.getRandomPartForEmail(), 3L);

        when(userService.findAll())
                .thenReturn(List.of(user1, user2, user3));

        List<UserDto> res = sendRequestFindAll();

        Assertions.assertEquals(3, res.size());
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

    private UserDto sendRequestGetUser(Long userId) throws Exception {
        MvcResult res = mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper
                .readValue(res.getResponse().getContentAsString(),
                        UserDto.class);
    }

    private void sendRequestDeleteUser(Long userId) throws Exception {
        mockMvc.perform(delete("/users/" + userId))
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


    private CreateUserDto getDefaultCreateUserDto(String random) {
        return CreateUserDto.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private User getDefaultUser(String random, Long id) {
        return User.builder()
                .id(id)
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private PatchUserDto getDefaultUpdateUser(String random) {
        return PatchUserDto.builder()
                .name("Update user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

}
