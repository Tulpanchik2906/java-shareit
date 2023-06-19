package ru.practicum.shareit.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.shareit.exception.ValidationException;


@ControllerAdvice
@Slf4j
public class ErrorHandler {
    private final ObjectMapper objectMapper;

    @Autowired
    public ErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(ValidationException exception)
            throws JsonProcessingException {
        return getResponseEntity(HttpStatus.BAD_REQUEST, exception);
    }

    private ResponseEntity<String> getResponseEntity(
            HttpStatus httpStatus, Exception exception)
            throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());
        log.error(exception.getMessage());
        return ResponseEntity
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(errorResponse));

    }
}
