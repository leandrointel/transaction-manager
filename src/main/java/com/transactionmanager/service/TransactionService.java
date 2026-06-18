package com.transactionmanager.service;

import com.transactionmanager.dto.StatusResponseDTO;
import com.transactionmanager.dto.SumResponseDTO;
import com.transactionmanager.dto.TransactionRequestDTO;
import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.exception.InvalidTransactionException;
import com.transactionmanager.exception.TransactionNotFoundException;
import com.transactionmanager.model.Transaction;
import com.transactionmanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public StatusResponseDTO save(long id, TransactionRequestDTO request) {
        TransactionType type;
        try {
            type = TransactionType.fromString(request.type());
        } catch (IllegalArgumentException e) {
            throw new InvalidTransactionException(e.getMessage());
        }

        if (Objects.nonNull(request.parentId()) && !repository.existsById(request.parentId())) {
            throw new InvalidTransactionException(
                    "Parent transaction with id " + request.parentId() + " does not exist");
        }

        repository.save(new Transaction(id, request.amount(), type, request.parentId()));
        return StatusResponseDTO.ok();
    }

    public List<Long> getIdsByType(String typeStr) {
        try {
            TransactionType type = TransactionType.fromString(typeStr);
            return repository.findByType(type).stream()
                    .map(Transaction::id)
                    .toList();
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public SumResponseDTO getSum(long id) {
        Transaction root = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return new SumResponseDTO(sumRecursive(root));
    }

    private double sumRecursive(Transaction transaction) {
        double childSum = repository.findByParentId(transaction.id()).stream()
                .mapToDouble(this::sumRecursive)
                .sum();
        return transaction.amount() + childSum;
    }
}
