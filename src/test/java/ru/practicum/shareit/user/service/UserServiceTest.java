package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotFoundException;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
    }


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

    @Test
    public void testUpdateUserSuccess() {
        User user = getDefaultUser(
                TestUtil.getRandomPartForEmail());
        User userSaved = userService.create(user);

        User userUpdate = getDefaultUpdateUser(
                TestUtil.getRandomPartForEmail());

        User userUpdateSaved = userService.update(
                userSaved.getId(), userUpdate);

        Assertions.assertEquals(userSaved.getId(), userUpdateSaved.getId());
        Assertions.assertEquals(userUpdate.getName(), userUpdateSaved.getName());
        Assertions.assertEquals(userUpdate.getEmail(), userUpdateSaved.getEmail());
    }

    @Test
    public void testUpdateUserFailedNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> userService.update(-1L,
                        getDefaultUpdateUser(TestUtil.getRandomPartForEmail())));
    }


    @Test
    public void testUpdateUserFailedDoubleEmail() {
        User user1 = userService.create(getDefaultUser(
                TestUtil.getRandomPartForEmail()));
        User user2 = userService.create(getDefaultUser(
                TestUtil.getRandomPartForEmail()));

        User userUpdate = getDefaultUpdateUser("1");
        userUpdate.setEmail(user1.getEmail());

        Assertions.assertThrows(Exception.class,
                () -> userService.update(user2.getId(), userUpdate));
    }

    @Test
    public void testUpdateUserSuccessIdenticalUpdate() {
        User user = getDefaultUser(TestUtil.getRandomPartForEmail());
        User userSaved = userService.create(user);

        User userUpdateSaved = userService.update(
                userSaved.getId(), userSaved);

        Assertions.assertEquals(userSaved.getId(), userUpdateSaved.getId());
        Assertions.assertEquals(userSaved.getName(), userUpdateSaved.getName());
        Assertions.assertEquals(userSaved.getEmail(), userUpdateSaved.getEmail());
    }

    @Test
    public void testUpdateUserSuccessUpdateNewEmail() {
        User user = getDefaultUser(TestUtil.getRandomPartForEmail());
        User userSaved = userService.create(user);
        userSaved.setEmail(TestUtil.getRandomPartForEmail());
        String saveName = userSaved.getName();
        userSaved.setName(null);

        User userUpdateSaved = userService.update(userSaved.getId(), userSaved);

        Assertions.assertEquals(userSaved.getId(), userUpdateSaved.getId());
        Assertions.assertEquals(saveName, userUpdateSaved.getName());
        Assertions.assertEquals(userSaved.getEmail(), userUpdateSaved.getEmail());
    }

    @Test
    public void testUpdateUserSuccessUpdateName() {
        User user = getDefaultUser(TestUtil.getRandomPartForEmail());
        User userSaved = userService.create(user);
        String email = userSaved.getEmail();
        userSaved.setName("New name");
        userSaved.setEmail(null);

        User userUpdateSaved = userService.update(userSaved.getId(), userSaved);

        Assertions.assertEquals(userSaved.getId(), userUpdateSaved.getId());
        Assertions.assertEquals(userSaved.getName(), userUpdateSaved.getName());
        Assertions.assertEquals(email, userUpdateSaved.getEmail());
    }

    /*
        Тесты на метод get(Long id)
     */
    @Test
    public void testGetUserSuccess() {
        User user = getDefaultUser(TestUtil.getRandomPartForEmail());
        User userSaved = userService.create(user);

        User userGet = userService.get(userSaved.getId());

        Assertions.assertEquals(user.getId(), userGet.getId());
        Assertions.assertEquals(user.getName(), userGet.getName());
        Assertions.assertEquals(user.getEmail(), userGet.getEmail());
    }

    @Test
    public void testGetUserFailedNoUser() {
        Assertions.assertThrows(NotFoundException.class,
                () -> userService.get(-1L));
    }

    /*
        Тесты на метод findAll()
     */

    @Test
    public void testFindAllSuccessEmptyList() {
        Assertions.assertEquals(0, userService.findAll().size());
    }

    @Test
    public void testFindAllSuccessNoEmptyList() {
        userService.create(getDefaultUser(TestUtil.getRandomPartForEmail()));
        userService.create(getDefaultUser(TestUtil.getRandomPartForEmail()));
        userService.create(getDefaultUser(TestUtil.getRandomPartForEmail()));

        Assertions.assertEquals(3, userService.findAll().size());
    }


    private User getDefaultUser(String random) {
        return User.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private User getDefaultUpdateUser(String random) {
        return User.builder()
                .name("Update user")
                .email("user" + random + "@yandex.ru")
                .build();
    }
}
