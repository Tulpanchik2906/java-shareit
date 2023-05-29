package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.PatchItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(UserMapper.toUserDto(item.getOwner()))
                .comments(item.getComments().stream()
                        .map(x -> CommentMapper.toCommentDto(x))
                        .collect(Collectors.toList()))
                .build();

        if (item.getRequest() != null) {
            itemDto.setRequest(RequestMapper.toItemRequestDto(item.getRequest()));
        }

        if (item.getLastBooking() != null) {
            itemDto.setLastBooking(BookingMapper.toBookingForItemDto(item.getLastBooking()));
        }

        if (item.getNextBooking() != null) {
            itemDto.setNextBooking(BookingMapper.toBookingForItemDto(item.getNextBooking()));
        }

        return itemDto;
    }

    public static Item toItem(CreateItemDto itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static Item toItem(PatchItemDto itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }
}
