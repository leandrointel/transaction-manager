package com.transactionmanager.service.impl;

import com.transactionmanager.dto.StatusResponseDTO;
import com.transactionmanager.dto.SumResponseDTO;
import com.transactionmanager.exception.InvalidTransactionException;
import com.transactionmanager.exception.TransactionNotFoundException;
import com.transactionmanager.helper.TransactionSumHelper;
import com.transactionmanager.model.Transaction;
import com.transactionmanager.persistence.TransactionRepository;
import com.transactionmanager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link TransactionService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final TransactionSumHelper sumHelper;

    @Override
    public StatusResponseDTO saveTransaction(long transactionId, double amount, String type, Long parentId) {
        log.debug("Saving transaction id={} type={} amount={} parentId={}", transactionId, type, amount, parentId);

        if (parentId != null) {
            repository.findById(parentId)
                    .orElseThrow(() -> new InvalidTransactionException(
                            "Parent transaction not found with id: " + parentId));
        }

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .amount(amount)
                .type(type)
                .parentId(parentId)
                .build();

        repository.save(transaction);
        return StatusResponseDTO.ok();
    }

    @Override
    public List<Long> getIdsByType(String type) {
        log.debug("Fetching ids for type={}", type);
        return repository.findAllByType(type).stream()
                .map(Transaction::getId)
                .toList();
    }

    @Override
    public SumResponseDTO getTransitiveSum(long transactionId) {
        log.debug("Computing transitive sum for id={}", transactionId);
        repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        double sum = sumHelper.computeTransitiveSum(transactionId, repository.findAll());
        return new SumResponseDTO(sum);
    }
}
