package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
public class CreateItemRequestDto {
    private String description; // текст запроса, содержащий описание требуемой вещи;

}
