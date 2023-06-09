package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class UserControllerWithMock {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    /*
    @BeforeEach
    public void beforeEach() {
        when(userService.create(any()))
                .thenReturn(User.builder()
                        .id(1L)
                        .name("Name user")
                        .email("user1@yandex.ru")
                        .build());
        when(userService.update(1L, any()))
                .thenReturn(User.builder()
                        .id(1L)
                        .name("Update user")
                        .email("userUpdate1@yandex.ru")
                        .build());

    }*/


    @Test
    public void testCreateUserSuccess() throws Exception {
        when(userService.create(any()))
                .thenReturn(User.builder()
                        .id(1L)
                        .name("Name user")
                        .email("user1@yandex.ru")
                        .build());
        sendRequestAddUser(
                getDefaultCreateUserDto(TestUtil.getRandomPartForEmail()));
        UserDto res = sendRequestAddUser(
                getDefaultCreateUserDto(TestUtil.getRandomPartForEmail()));

        //Assertions.assertEquals(1L, res.getId());
        Assertions.assertEquals("Name user", res.getName());
       // Assertions.assertEquals("user1@yandex.ru", res.getEmail());
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
