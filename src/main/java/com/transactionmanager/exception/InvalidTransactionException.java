package com.transactionmanager.exception;

/**
 * Thrown when a transaction request contains invalid or inconsistent data.
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
