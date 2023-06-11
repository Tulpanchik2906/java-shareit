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
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerWithMock {


    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UserServiceImpl userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserServiceImpl.class);

        userController = new UserController(userService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

    }

    @Test
    public void testCreateUserSuccess() throws Exception {
        CreateUserDto createUserDto = getDefaultCreateUserDto(TestUtil.getRandomPartForEmail());

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

    private CreateUserDto getDefaultCreateUserDto(String random) {
        return CreateUserDto.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }
}
