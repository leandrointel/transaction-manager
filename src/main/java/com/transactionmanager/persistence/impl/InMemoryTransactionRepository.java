package com.transactionmanager.persistence.impl;

import com.transactionmanager.model.Transaction;
import com.transactionmanager.persistence.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link TransactionRepository}.
 * Uses a {@link ConcurrentHashMap} keyed by transaction id.
 */
@Slf4j
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> store = new ConcurrentHashMap<>();

    @Override
    public void save(Transaction transaction) {
        log.debug("Saving transaction id={}", transaction.getId());
        store.put(transaction.getId(), transaction);
    }

    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Transaction> findAllByType(String type) {
        return store.values().stream()
                .filter(t -> t.getType().equals(type))
                .toList();
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void clear() {
        store.clear();
    }
}
