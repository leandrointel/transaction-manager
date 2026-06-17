package com.transactionmanager.persistence;

import com.transactionmanager.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for transaction storage operations.
 * Decouples service layer from persistence implementation details.
 */
public interface TransactionRepository {

    /**
     * Saves a transaction, overwriting any existing transaction with the same id.
     *
     * @param transaction the transaction to persist
     */
    void save(Transaction transaction);

    /**
     * Finds a transaction by its unique id.
     *
     * @param id the transaction id
     * @return Optional containing the transaction, or empty if not found
     */
    Optional<Transaction> findById(long id);

    /**
     * Returns all transactions of the given type.
     *
     * @param type the type to filter by
     * @return list of matching transactions; never null
     */
    List<Transaction> findAllByType(String type);

    /**
     * Returns all transactions currently stored.
     *
     * @return immutable snapshot of all transactions
     */
    List<Transaction> findAll();

    /**
     * Removes all stored transactions. Intended for test isolation only.
     */
    void clear();
}
