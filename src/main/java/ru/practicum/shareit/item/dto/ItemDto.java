package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;// уникальный идентификатор вещи;
    private String name; // краткое название;
    private String description; // развёрнутое описание;
    private Boolean available; // статус о том, доступна или нет вещь для аренды;
    private UserDto owner; // владелец вещи;
    private Long requestId; // если вещь была создана по запросу другого пользователя,
    // то в этом поле будет храниться ссылка на соответствующий запрос
    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
    private List<CommentDto> comments;

}
