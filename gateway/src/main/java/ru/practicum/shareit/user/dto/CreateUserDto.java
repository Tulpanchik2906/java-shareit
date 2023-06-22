package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {
    @NotNull
    @NotBlank
    private String name; // имя или логин пользователя;
    @Email
    @NotNull
    private String email; // адрес электронной почты (учтите, что два пользователя не могут
    //иметь одинаковый адрес электронной почты).
}
