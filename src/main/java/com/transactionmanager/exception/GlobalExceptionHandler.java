package com.transactionmanager.exception;

import com.transactionmanager.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler that converts domain exceptions into standardized HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles requests for a transaction id that does not exist in the repository.
     *
     * @param ex the exception carrying the missing transaction id
     * @return 404 Not Found with a structured error body
     */
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(TransactionNotFoundException ex) {
        log.warn("Transaction not found: id={}", ex.getTransactionId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDTO.of(404, "Not Found", ex.getMessage()));
    }

    /**
     * Handles business rule violations such as an unknown transaction type or a missing parent.
     *
     * @param ex the exception describing the validation failure
     * @return 400 Bad Request with a structured error body
     */
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalid(InvalidTransactionException ex) {
        log.warn("Invalid transaction request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.of(400, "Bad Request", ex.getMessage()));
    }

    /**
     * Handles Bean Validation failures on request bodies (e.g. missing required fields).
     * Only the first field error is included in the response message.
     *
     * @param ex the exception produced by the validation framework
     * @return 400 Bad Request with a structured error body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        log.warn("Validation failure: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.of(400, "Bad Request", message));
    }

    /**
     * Catch-all handler for any unhandled exception. Logs the full stack trace and returns
     * a generic 500 response to avoid leaking internal details to the client.
     *
     * @param ex the unexpected exception
     * @return 500 Internal Server Error with a generic error body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.of(500, "Internal Server Error", "An unexpected error occurred"));
    }
}
