package com.transactionmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming transaction creation/update requests.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {

    /** Transaction amount. Must not be null. */
    @NotNull(message = "amount is required")
    private Double amount;

    /** Transaction type identifier. Must not be blank. */
    @NotBlank(message = "type is required")
    private String type;

    /** Optional id of the parent transaction. */
    @JsonProperty("parent_id")
    private Long parentId;
}
