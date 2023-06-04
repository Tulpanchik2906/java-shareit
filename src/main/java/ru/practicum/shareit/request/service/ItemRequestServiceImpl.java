package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequest create(ItemRequest itemRequest, Long userId) {
        userRepository.getExistUser(userId);
        itemRequest.setRequester(userRepository.getExistUser(userId));
        itemRequest.setCreated(LocalDateTime.now());
        return setItems(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequest> findAllByUserId(Long userId) {
        userRepository.getExistUser(userId);
        return setItems(itemRequestRepository.findByRequesterId(userId));
    }

    @Override
    public List<ItemRequest> findAllByOffset(Long userId, int from, int size) {
        userRepository.getExistUser(userId);
        //TODO: Разобраться с пагинацией
        return setItems(itemRequestRepository.findByRequesterId(userId,
                PageRequest.of(from, size)));

    }

    private ItemRequest setItems(ItemRequest itemRequest) {
        itemRequest.setItems(
                itemRepository.findByRequestId(itemRequest.getId()));
        return itemRequest;
    }

    private List<ItemRequest> setItems(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(this::setItems)
                .collect(Collectors.toList());
    }
}
