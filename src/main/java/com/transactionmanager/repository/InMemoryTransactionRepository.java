package com.transactionmanager.repository;

import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> store = new ArrayList<>();

    @Override
    public synchronized void save(Transaction transaction) {
        store.removeIf(t -> t.id() == transaction.id());
        store.add(transaction);
    }

    @Override
    public synchronized Optional<Transaction> findById(long id) {
        return store.stream()
                .filter(t -> t.id() == id)
                .findFirst();
    }

    @Override
    public synchronized List<Transaction> findByType(TransactionType type) {
        return store.stream()
                .filter(t -> t.type() == type)
                .toList();
    }

    @Override
    public synchronized List<Transaction> findByParentId(long parentId) {
        return store.stream()
                .filter(t -> t.parentId() != null && t.parentId() == parentId)
                .toList();
    }

    @Override
    public synchronized boolean existsById(long id) {
        return store.stream().anyMatch(t -> t.id() == id);
    }
}
