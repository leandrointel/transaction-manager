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

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> store = new HashMap<>();
    private final Map<Long, List<Long>> childrenIndex = new HashMap<>();

    @Override
    public synchronized void save(Transaction transaction) {
        Transaction existing = store.get(transaction.id());
        if (Objects.nonNull(existing)) {
            removeFromChildrenIndex(existing);
        }
        store.put(transaction.id(), transaction);
        addToChildrenIndex(transaction);
    }

    @Override
    public synchronized Optional<Transaction> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public synchronized List<Transaction> findByType(TransactionType type) {
        return store.values().stream()
                .filter(t -> t.type() == type)
                .toList();
    }

    @Override
    public synchronized List<Transaction> findByParentId(long parentId) {
        return childrenIndex.getOrDefault(parentId, List.of()).stream()
                .map(store::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public synchronized boolean existsById(long id) {
        return store.containsKey(id);
    }

    private void removeFromChildrenIndex(Transaction transaction) {
        if (Objects.nonNull(transaction.parentId())) {
            List<Long> siblings = childrenIndex.get(transaction.parentId());
            if (Objects.nonNull(siblings)) {
                siblings.remove(transaction.id());
            }
        }
    }

    private void addToChildrenIndex(Transaction transaction) {
        if (Objects.nonNull(transaction.parentId())) {
            childrenIndex
                    .computeIfAbsent(transaction.parentId(), k -> new ArrayList<>())
                    .add(transaction.id());
        }
    }
}
