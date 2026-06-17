package com.transactionmanager.dto;

import com.transactionmanager.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming transaction creation/update requests.
 * Snake_case mapping is handled globally by Jackson — no field-level annotations needed.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {

    /** Transaction amount. Must not be null. */
    @NotNull(message = "amount is required")
    private Double amount;

    /**
     * Transaction type. Must match one of the values in {@link TransactionType}
     * (case-insensitive). Validated as a string here; converted to enum in the service layer.
     */
    @NotBlank(message = "type is required")
    private String type;

    /** Optional id of the parent transaction. Mapped from snake_case via global Jackson config. */
    private Long parentId;
}
