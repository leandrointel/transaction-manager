package com.transactionmanager.dto;

import com.transactionmanager.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for incoming transaction creation/update requests.
 * Snake_case mapping (parent_id) handled globally by Jackson.
 * Bean Validation annotations are supported on record components since Jakarta Validation 3.
 */
public record TransactionRequestDTO(

        @NotNull(message = "amount is required")
        Double amount,

        /**
         * Transaction type. Must match one of the values in {@link TransactionType}
         * (case-insensitive). Converted to enum in the service layer.
         */
        @NotBlank(message = "type is required")
        String type,

        Long parentId
) {}
