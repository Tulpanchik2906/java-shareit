package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id; // уникальный идентификатор запроса;
    private String description; // текст запроса, содержащий описание требуемой вещи;
    private User requestor; // пользователь, создавший запрос;
    private LocalDateTime created; // дата и время создания запроса.
    private List<ItemForRequestDto> items;
}
