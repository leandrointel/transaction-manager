package com.transactionmanager.repository;

import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Contract for transaction persistence. Implementations may use any backing store
 * (in-memory, relational database, Redis, etc.) without affecting the service layer.
 */
public interface TransactionRepository {

    /**
     * Persists a transaction. If a transaction with the same id already exists it is replaced,
     * including any index updates derived from its parent relationship.
     *
     * @param transaction the transaction to store
     */
    void save(Transaction transaction);

    /**
     * Looks up a transaction by its unique identifier.
     *
     * @param id the transaction id
     * @return an {@link Optional} containing the transaction, or empty if not found
     */
    Optional<Transaction> findById(long id);

    /**
     * Returns all transactions whose type matches the given value.
     *
     * @param type the transaction type to filter by
     * @return a list of matching transactions; empty list if none match
     */
    List<Transaction> findByType(TransactionType type);

    /**
     * Returns the direct children of a transaction, i.e. all transactions whose
     * {@code parentId} equals the given id.
     *
     * @param parentId the id of the parent transaction
     * @return a list of direct children; empty list if the transaction has no children
     */
    List<Transaction> findByParentId(long parentId);

    /**
     * Checks whether a transaction with the given id exists.
     *
     * @param id the transaction id
     * @return {@code true} if the transaction exists, {@code false} otherwise
     */
    boolean existsById(long id);
}
