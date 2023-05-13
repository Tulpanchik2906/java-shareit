package ru.practicum.shareit.util.handler;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    // название ошибки
    private String error;
    // подробное описание
    private String description;

}
