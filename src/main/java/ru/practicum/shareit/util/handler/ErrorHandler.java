package ru.practicum.shareit.util.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.shareit.util.exception.*;


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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException exception)
            throws JsonProcessingException {
        return getResponseEntity(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler({DuplicateEmailException.class,
            JdbcSQLIntegrityConstraintViolationException.class})
    public ResponseEntity<String> handleDuplicateEmailException(Exception exception)
            throws JsonProcessingException {
        return getResponseEntity(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(NotAvailableItemException.class)
    public ResponseEntity<String> handleNotAvailableItemException(NotAvailableItemException exception)
            throws JsonProcessingException {
        return getResponseEntity(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(ApproveBookingException.class)
    public ResponseEntity<String> handleApproveBookingExceptionException(ApproveBookingException exception)
            throws JsonProcessingException {
        return getResponseEntity(HttpStatus.FORBIDDEN, exception);
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
