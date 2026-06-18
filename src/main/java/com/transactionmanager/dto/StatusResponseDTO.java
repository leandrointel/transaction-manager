package com.transactionmanager.dto;

/**
 * Generic status response for operations that return a simple status string.
 */
public record StatusResponseDTO(String status) {

    public static StatusResponseDTO ok() {
        return new StatusResponseDTO("ok");
    }
}
