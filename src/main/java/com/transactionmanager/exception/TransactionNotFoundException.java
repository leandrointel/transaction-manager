package com.transactionmanager.exception;

/**
 * Thrown when a requested transaction does not exist in the repository.
 */
public class TransactionNotFoundException extends RuntimeException {

    private final long transactionId;

    public TransactionNotFoundException(long transactionId) {
        super("Transaction not found with id: " + transactionId);
        this.transactionId = transactionId;
    }

    public long getTransactionId() {
        return transactionId;
    }
}
