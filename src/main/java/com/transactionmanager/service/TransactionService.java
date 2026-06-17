package com.transactionmanager.service;

import com.transactionmanager.dto.StatusResponseDTO;
import com.transactionmanager.dto.SumResponseDTO;

import java.util.List;

/**
 * Service interface defining the business operations for transactions.
 */
public interface TransactionService {

    /**
     * Creates or updates a transaction.
     *
     * @param transactionId unique identifier for the transaction
     * @param amount        monetary amount
     * @param type          transaction type
     * @param parentId      optional parent transaction id; null if root
     * @return status response
     */
    StatusResponseDTO saveTransaction(long transactionId, double amount, String type, Long parentId);

    /**
     * Returns the ids of all transactions of the specified type.
     *
     * @param type the type to query
     * @return list of transaction ids; never null
     */
    List<Long> getIdsByType(String type);

    /**
     * Computes the transitive sum of all transactions linked to the given id.
     *
     * @param transactionId root transaction id
     * @return sum response DTO
     */
    SumResponseDTO getTransitiveSum(long transactionId);
}
