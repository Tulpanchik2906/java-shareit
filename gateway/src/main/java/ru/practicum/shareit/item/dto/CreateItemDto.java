package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemDto {
    @NotNull
    @NotBlank
    private String name; // краткое название;

    @NotNull
    @NotBlank
    private String description; // развёрнутое описание;

    @NotNull
    private Boolean available; // статус о том, доступна или нет вещь для аренды;

    private Long requestId;

}
