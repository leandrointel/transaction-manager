package com.transactionmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Generic status response DTO for operations that return a simple status.
 */
@Getter
@AllArgsConstructor
public class StatusResponseDTO {

    private final String status;

    public static StatusResponseDTO ok() {
        return new StatusResponseDTO("ok");
    }
}
