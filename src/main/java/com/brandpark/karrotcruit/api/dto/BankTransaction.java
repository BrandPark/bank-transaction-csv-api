package com.brandpark.karrotcruit.api.dto;

import com.brandpark.karrotcruit.api.BankCode;
import com.brandpark.karrotcruit.api.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Locale;

@NoArgsConstructor
@Getter
@Entity
public class BankTransaction {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "bank_transaction_id")
    private Long id;

    private int year;

    private int month;

    private int day;

    private Long userId;

    private BankCode bankCode;

    private long transactionAmount;

    private TransactionType transactionType;

    public static BankTransaction createBankTransaction(String[] split) {
        BankTransaction bt = new BankTransaction();
        bt.id = Long.parseLong(split[0]);
        bt.year = Integer.parseInt(split[1]);
        bt.month = Integer.parseInt(split[2]);
        bt.day = Integer.parseInt(split[3]);
        bt.userId = Long.parseLong(split[4]);
        bt.bankCode = BankCode.of(split[5]);
        bt.transactionAmount = Integer.parseInt(split[6]);
        bt.transactionType = TransactionType.valueOf(split[7].toUpperCase(Locale.ROOT));

        return bt;
    }
}
