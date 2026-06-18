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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Business logic for the transaction management domain.
 * Delegates persistence to {@link TransactionRepository} and remains agnostic of the backing store.
 */
@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates or replaces a transaction.
     *
     * <p>Validates the type string against {@link TransactionType} (case-insensitive) and, when a
     * parent id is provided, verifies that the parent transaction already exists.
     *
     * @param id      the transaction id, supplied by the caller via the URL path
     * @param request the request payload containing amount, type, and optional parent id
     * @return a status response with {@code "ok"} on success
     * @throws InvalidTransactionException if the type is not a known {@link TransactionType} value
     *                                     or the referenced parent does not exist
     */
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

    /**
     * Returns the ids of all transactions whose type matches the given string.
     *
     * <p>The comparison is case-insensitive. An unrecognized type string returns an empty list
     * rather than an error, so callers receive a consistent array response for any input.
     *
     * @param typeStr the raw type string from the request path
     * @return a list of transaction ids; empty if no match or the type is unknown
     */
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

    /**
     * Computes the transitive sum of a transaction and all its descendants.
     *
     * <p>Traverses the tree top-down starting from the given id, accumulating the {@code amount}
     * of every node reachable through parent-child relationships.
     *
     * @param id the root transaction id
     * @return a {@link SumResponseDTO} containing the total sum
     * @throws TransactionNotFoundException if no transaction with the given id exists
     */
    public SumResponseDTO getSum(long id) {
        Transaction root = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        double total = Stream.iterate(
                        List.of(root),
                        level -> !level.isEmpty(),
                        level -> level.stream()
                                .flatMap(t -> repository.findByParentId(t.id()).stream())
                                .toList())
                .flatMap(Collection::stream)
                .mapToDouble(Transaction::amount)
                .sum();

        return new SumResponseDTO(total);
    }
}
