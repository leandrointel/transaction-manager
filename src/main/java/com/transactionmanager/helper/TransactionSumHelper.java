package com.transactionmanager.helper;

import com.transactionmanager.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Helper responsible for computing the transitive sum of a transaction tree.
 * Encapsulates the recursive/stream traversal logic away from the service layer.
 */
@Component
public class TransactionSumHelper {

    /**
     * Computes the sum of {@code root} amount plus all transactions that are
     * transitively linked to it via {@code parent_id}, using the provided full list.
     *
     * @param rootId       the id of the root transaction
     * @param allTransactions full list of stored transactions
     * @return the transitive sum
     */
    public double computeTransitiveSum(long rootId, List<Transaction> allTransactions) {
        return allTransactions.stream()
                .filter(t -> isDescendantOrSelf(t, rootId, allTransactions))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private boolean isDescendantOrSelf(Transaction candidate, long rootId, List<Transaction> all) {
        if (candidate.getId() == rootId) {
            return true;
        }
        return candidate.getOptionalParentId()
                .map(pid -> {
                    Transaction parent = all.stream()
                            .filter(t -> t.getId() == pid)
                            .findFirst()
                            .orElse(null);
                    return parent != null && isDescendantOrSelf(parent, rootId, all);
                })
                .orElse(false);
    }
}
