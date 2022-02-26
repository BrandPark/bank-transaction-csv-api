package com.brandpark.karrotcruit.api.bank_transaction.dto;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransaction;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Data
public class BankTransactionResponse {
    private Long bankTransactionId;
    private Long userId;
    private String bankCode;
    private String transactionType;
    private String transactionDate;
    private long transactionAmount;

    public BankTransactionResponse(BankTransaction entity) {
        bankTransactionId = entity.getId();
        userId = entity.getUserId();
        bankCode = entity.getBankCode().getCode();
        transactionType = entity.getTransactionType().name();
        transactionDate = entity.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        transactionAmount = entity.getTransactionAmount();
    }
}
