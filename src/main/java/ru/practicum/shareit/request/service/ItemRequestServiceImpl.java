package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.util.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequest create(ItemRequest itemRequest, Long userId) {
        validateExistUser(userId);
        itemRequest.setRequester(userRepository.findById(userId).get());
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(itemRequest);
    }

    @Override
    public List<ItemRequest> findAllByUserId(Long userId) {
        validateExistUser(userId);
        return itemRequestRepository.findByRequesterId(userId);
    }

    @Override
    public List<ItemRequest> findAllByOffset(Long userId, int from, int size) {
        validateExistUser(userId);
        //TODO: Разобраться с пагинацией
        //return itemRequestRepository.findAll(PageRequest.of(from, size)).toList();
        return new ArrayList<>();
    }

    private void validateExistUser(Long userId) {
        if (userId == null || !userRepository.findById(userId).isPresent()) {
            log.error("Пользователь с id: {} не найден.", userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден.");
        }
    }
}
