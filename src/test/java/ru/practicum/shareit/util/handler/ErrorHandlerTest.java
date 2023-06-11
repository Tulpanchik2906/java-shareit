package ru.practicum.shareit.util.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.util.exception.NotAvailableItemException;
import ru.practicum.shareit.util.exception.NotFoundException;
import ru.practicum.shareit.util.exception.ValidationException;

public class ErrorHandlerTest {

    private ObjectMapper objectMapper;

    private ErrorHandler errorHandler;

    @BeforeEach
    public void before() {
        objectMapper = new ObjectMapper();
        errorHandler = new ErrorHandler(objectMapper);
    }

    @Test
    public void testHandleValidationException() throws JsonProcessingException {
        String errorMessage = "Validation exception";
        ResponseEntity<String> errorEntity = errorHandler.handleValidationException(
                new ValidationException(errorMessage));

        checkError(errorMessage, errorEntity, HttpStatus.BAD_REQUEST);

    }

    @Test
    public void testHandleNotFoundException() throws JsonProcessingException {
        String errorMessage = "Not found exception";
        ResponseEntity<String> errorEntity = errorHandler.handleNotFoundException(
                new NotFoundException(errorMessage));

        checkError(errorMessage, errorEntity, HttpStatus.NOT_FOUND);

    }

    @Test
    public void testHandleDuplicateEmailException() throws JsonProcessingException {
        String errorMessage = "Duplicate email";
        ResponseEntity<String> errorEntity = errorHandler.handleDuplicateEmailException(
                new JdbcSQLIntegrityConstraintViolationException(
                        errorMessage, "sql", "state", 409, new Throwable(), "stackTrace"));

        errorMessage = errorMessage + "; SQL statement:\n" +
                "sql [409-214]";
        checkError(errorMessage, errorEntity, HttpStatus.CONFLICT);

    }

    @Test
    public void testHandleNotAvailableItemException() throws JsonProcessingException {
        String errorMessage = "Not available item";
        ResponseEntity<String> errorEntity = errorHandler.handleNotAvailableItemException(
                new NotAvailableItemException(errorMessage));

        checkError(errorMessage, errorEntity, HttpStatus.BAD_REQUEST);

    }

    private void checkError(
            String errorMessage, ResponseEntity<String> errorEntity, HttpStatus httpStatus)
            throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);

        Assertions.assertEquals(httpStatus, errorEntity.getStatusCode());
        Assertions.assertEquals(errorResponse, objectMapper
                .readValue(errorEntity.getBody(), ErrorResponse.class));
    }
}
