package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.TestUtil;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void afterEach() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testSearchNameWithOutPageable() {
        preSearchNameTest();

        Assertions.assertEquals(2, itemRepository.search("Name").size());
    }

    @Test
    public void testSearchName() {
        preSearchNameTest();

        Assertions.assertEquals(2, itemRepository.search("Name",
                PageRequest.of(0, 3)).size());
    }

    @Test
    public void testSearchDescriptionWithOutPageable() {
        preSearchDescriptionTest();

        Assertions.assertEquals(2, itemRepository.search("description").size());
    }

    @Test
    public void testSearchDescription() {
        preSearchDescriptionTest();

        Assertions.assertEquals(2, itemRepository.search("description",
                PageRequest.of(0, 3)).size());
    }

    private void preSearchDescriptionTest() {
        User user = addUser();

        Item item1 = getDefaultItem(user);
        Item item2 = getDefaultItem(user);
        Item item3 = getDefaultItem(user);

        item1.setDescription("Lala");

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
    }

    @Test
    public void testSearchAvailable() {
        preTestSearchAvailable();

        Assertions.assertEquals(2, itemRepository.search("description").size());
    }

    private void preSearchNameTest() {
        User user = addUser();

        Item item1 = getDefaultItem(user);
        Item item2 = getDefaultItem(user);
        Item item3 = getDefaultItem(user);

        item1.setName("Lala");

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
    }

    private void preTestSearchAvailable() {
        User user = addUser();

        Item item1 = getDefaultItem(user);
        Item item2 = getDefaultItem(user);
        Item item3 = getDefaultItem(user);

        item1.setAvailable(false);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
    }

    private User addUser() {
        return userRepository.save(getDefaultUser(TestUtil.getRandomPartForEmail()));
    }


    private User getDefaultUser(String random) {
        return User.builder()
                .name("Name user")
                .email("user" + random + "@yandex.ru")
                .build();
    }

    private Item getDefaultItem(User user) {
        return Item.builder()
                .available(true)
                .name("New item name")
                .description("New item description")
                .owner(user)
                .build();
    }
}
