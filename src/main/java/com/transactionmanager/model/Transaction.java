package com.transactionmanager.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

/**
 * Internal domain model representing a financial transaction.
 * Never exposed directly via API — use DTOs instead.
 */
@Getter
@Builder
public class Transaction {

    private final long id;
    private final double amount;
    private final String type;
    private final Long parentId;

    /**
     * Returns the parent id wrapped in an Optional to enforce null-safety.
     *
     * @return Optional containing the parent id, or empty if this is a root transaction.
     */
    public Optional<Long> getOptionalParentId() {
        return Optional.ofNullable(parentId);
    }
}
