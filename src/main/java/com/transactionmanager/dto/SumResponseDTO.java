package com.transactionmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO for the transitive sum response of a transaction tree.
 */
@Getter
@AllArgsConstructor
public class SumResponseDTO {

    private final double sum;
}
