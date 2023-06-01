package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.util.UserUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserUtil userUtil;

    @Override
    public ItemRequest create(ItemRequest itemRequest, Long userId) {
        userUtil.getExistUser(userId);
        itemRequest.setRequester(userUtil.getExistUser(userId));
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(itemRequest);
    }

    @Override
    public List<ItemRequest> findAllByUserId(Long userId) {
        userUtil.getExistUser(userId);
        return itemRequestRepository.findByRequesterId(userId,
                PageRequest.of(0, 100));
    }

    @Override
    public List<ItemRequest> findAllByOffset(Long userId, int from, int size) {
        userUtil.getExistUser(userId);
        //TODO: Разобраться с пагинацией
        return itemRequestRepository.findByRequesterId(userId,
                PageRequest.of(from, size));

    }
}
