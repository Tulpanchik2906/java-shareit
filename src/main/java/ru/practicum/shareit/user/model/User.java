package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class User {
    private Long id; // уникальный идентификатор пользователя;
    private String name; // имя или логин пользователя;
    @Email
    private String email; // адрес электронной почты (учтите, что два пользователя не могут
    //иметь одинаковый адрес электронной почты).

}
