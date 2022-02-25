package com.brandpark.karrotcruit.api.bankTransaction;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionType {

    WITHDRAW("출금"),
    DEPOSIT("입금");

    private final String typeName;
}
