package com.transactionmanager.dto;

import java.time.Instant;

/**
 * Standardized error response returned by the global exception handler.
 */
public record ErrorResponseDTO(int status, String error, String message, Instant timestamp) {

    public static ErrorResponseDTO of(int status, String error, String message) {
        return new ErrorResponseDTO(status, error, message, Instant.now());
    }
}
