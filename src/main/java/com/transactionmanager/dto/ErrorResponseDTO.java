package com.transactionmanager.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Standardized error response DTO returned by the global exception handler.
 */
@Getter
@Builder
public class ErrorResponseDTO {

    private final int status;
    private final String error;
    private final String message;
    private final Instant timestamp;

    public static ErrorResponseDTO of(int status, String error, String message) {
        return ErrorResponseDTO.builder()
                .status(status)
                .error(error)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
