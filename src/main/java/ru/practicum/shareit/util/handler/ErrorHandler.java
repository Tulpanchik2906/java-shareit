package ru.practicum.shareit.util.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.shareit.util.exception.DuplicateEmailException;
import ru.practicum.shareit.util.exception.ValidationException;


@ControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(ValidationException e) {
        log.error("Ошибка валидации: " + e.getMessage());
        return new ErrorResponse(
                "Ошибка валидации", e.getMessage()
        );
    }

    @ExceptionHandler({NullPointerException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final NullPointerException e) {
        log.error("Объект не найден: " + e.getMessage());
        return new ErrorResponse(
                "Обънект не найден", e.getMessage()
        );
    }

    @ExceptionHandler({DuplicateEmailException.class})
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public ErrorResponse handle(final DuplicateEmailException e) {
        log.error("Повторный email", e.getMessage());
        return new ErrorResponse(
                "Повторный email", e.getMessage()
        );
    }
}
