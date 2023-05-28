package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
public class ItemDto {
    private Long id;// уникальный идентификатор вещи;
    private String name; // краткое название;
    private String description; // развёрнутое описание;
    private Boolean available; // статус о том, доступна или нет вещь для аренды;
    private UserDto owner; // владелец вещи;
    private ItemRequestDto request; // если вещь была создана по запросу другого пользователя,
    // то в этом поле будет храниться ссылка на соответствующий запрос
    private BookingDto lastBooking;
    private BookingDto nextBooking;

}
