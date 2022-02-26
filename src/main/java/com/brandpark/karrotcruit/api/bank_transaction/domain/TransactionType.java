package com.brandpark.karrotcruit.api.bank_transaction.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionType {

    WITHDRAW("출금"),
    DEPOSIT("입금");

    private final String typeName;
}
