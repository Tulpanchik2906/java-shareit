package ru.practicum.shareit.util.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
