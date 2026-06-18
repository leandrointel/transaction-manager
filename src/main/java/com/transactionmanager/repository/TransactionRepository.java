package com.transactionmanager.repository;

import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    void save(Transaction transaction);

    Optional<Transaction> findById(long id);

    List<Transaction> findByType(TransactionType type);

    List<Transaction> findByParentId(long parentId);

    boolean existsById(long id);
}
