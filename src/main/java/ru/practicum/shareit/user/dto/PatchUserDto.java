package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
@Builder
public class PatchUserDto {
    private String name; // имя или логин пользователя;
    @Email
    private String email; // адрес электронной почты (учтите, что два пользователя не могут
    //иметь одинаковый адрес электронной почты).
}
