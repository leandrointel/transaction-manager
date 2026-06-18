package com.transactionmanager.model;

import com.transactionmanager.enums.TransactionType;

public record Transaction(long id, double amount, TransactionType type, Long parentId) {}
