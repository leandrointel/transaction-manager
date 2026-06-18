package com.transactionmanager.repository;

import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-memory implementation of {@link TransactionRepository} backed by a {@link HashMap}.
 *
 * <p>A secondary {@code childrenIndex} maps each parent id to the list of its direct children ids,
 * making {@link #findByParentId} O(1) instead of a full-scan. Both structures are kept in sync
 * on every {@link #save}, including parent reassignment on overwrite.
 *
 * <p>All public methods are {@code synchronized} to provide basic thread safety for concurrent requests.
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> store = new HashMap<>();
    private final Map<Long, List<Long>> childrenIndex = new HashMap<>();

    /**
     * {@inheritDoc}
     *
     * <p>If a transaction with the same id already exists, it is removed from its previous parent's
     * children index before the new version is stored and indexed under its new parent.
     */
    @Override
    public synchronized void save(Transaction transaction) {
        Transaction existing = store.get(transaction.id());
        if (Objects.nonNull(existing)) {
            removeFromChildrenIndex(existing);
        }
        store.put(transaction.id(), transaction);
        addToChildrenIndex(transaction);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lookup is O(1) via the backing {@link HashMap}.
     */
    @Override
    public synchronized Optional<Transaction> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Streams over all stored values; O(n) where n is the total number of transactions.
     */
    @Override
    public synchronized List<Transaction> findByType(TransactionType type) {
        return store.values().stream()
                .filter(t -> t.type() == type)
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lookup is O(1) via the children index; resolving each child id to a {@link Transaction}
     * is also O(1) per child.
     */
    @Override
    public synchronized List<Transaction> findByParentId(long parentId) {
        return childrenIndex.getOrDefault(parentId, List.of()).stream()
                .map(store::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lookup is O(1) via the backing {@link HashMap}.
     */
    @Override
    public synchronized boolean existsById(long id) {
        return store.containsKey(id);
    }

    /**
     * Removes a transaction from its parent's entry in the children index.
     * No-op if the transaction has no parent.
     *
     * @param transaction the transaction to deindex
     */
    private void removeFromChildrenIndex(Transaction transaction) {
        if (Objects.nonNull(transaction.parentId())) {
            List<Long> siblings = childrenIndex.get(transaction.parentId());
            if (Objects.nonNull(siblings)) {
                siblings.remove(transaction.id());
            }
        }
    }

    /**
     * Registers a transaction as a child of its parent in the children index.
     * No-op if the transaction has no parent.
     *
     * @param transaction the transaction to index
     */
    private void addToChildrenIndex(Transaction transaction) {
        if (Objects.nonNull(transaction.parentId())) {
            childrenIndex
                    .computeIfAbsent(transaction.parentId(), k -> new ArrayList<>())
                    .add(transaction.id());
        }
    }
}
