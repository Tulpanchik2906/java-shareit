package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.user.model.User;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    /*
        Тесты на метод create(User user)
     */

    @Test
    public void testCreateUserSuccess() {
        User user = getDefaultUser(TestUtil.getRandomPartForEmail());
        User userSaved = userService.create(user);

        Assertions.assertNotNull(userSaved.getId());
        Assertions.assertEquals(user.getName(), userSaved.getName());
        Assertions.assertEquals(user.getEmail(), userSaved.getEmail());
    }

    @Test
    public void testGetErrorCreateUserWithReplayEmail() {
        String email = TestUtil.getRandomPartForEmail();
        User user = getDefaultUser(email);
        userService.create(user);

        Assertions.assertThrows(Exception.class,
                () -> userService.create(getDefaultUser(email)));
    }

    /*
        Тесты на метод update(Long userId, User userPatch)
     */



    private User getDefaultUser(String random) {
        return User.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private User getAllFieldsForPatchUser(String random) {
        return User.builder()
                .name("Update user")
                .email("user" + random + "@yandex.ru")
                .build();
    }
}
