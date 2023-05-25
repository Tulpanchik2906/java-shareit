package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

public class RequestMapper {
    public static ItemRequest toItemRequest(CreateItemRequestDto createItemRequestDto) {
        return ItemRequest.builder()
                .description(createItemRequestDto.getDescription())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestor(itemRequest.getRequester())
                .created(itemRequest.getCreated())
                .build();
    }
}
