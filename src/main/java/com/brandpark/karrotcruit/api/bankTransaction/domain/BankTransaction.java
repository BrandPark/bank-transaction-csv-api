package com.brandpark.karrotcruit.api.bankTransaction.domain;

import com.brandpark.karrotcruit.api.bankTransaction.BankCodeConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Locale;

@NoArgsConstructor
@Getter
@Table(name = "bank_transaction")
@Entity
public class BankTransaction implements Persistable<Long> {

    @Id
    @Column(name = "bank_transaction_id")
    private Long id;

    @Column(name="year", nullable = false)
    private int year;

    @Column(name="month", nullable = false)
    private int month;

    @Column(name="day", nullable = false)
    private int day;

    @Column(name="transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Convert(converter = BankCodeConverter.class)
    @Column(name="bank_code", nullable = false)
    private BankCode bankCode;

    @Column(name="transaction_amount", nullable = false)
    private long transactionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name="transaction_type", nullable = false)
    private TransactionType transactionType;

    public static BankTransaction csvRowToEntity(String[] split) {
        BankTransaction bt = new BankTransaction();

        bt.id = Long.parseLong(split[0]);
        bt.year = Integer.parseInt(split[1]);
        bt.month = Integer.parseInt(split[2]);
        bt.day = Integer.parseInt(split[3]);
        bt.userId = Long.parseLong(split[4]);
        bt.bankCode = BankCode.of(split[5]);
        bt.transactionAmount = Integer.parseInt(split[6]);
        bt.transactionType = TransactionType.valueOf(split[7].toUpperCase(Locale.ROOT));
        bt.transactionDate = LocalDate.of(bt.year, bt.month, bt.day);

        return bt;
    }

    @Transient
    private boolean isNew = true;

    @PrePersist
    @PostLoad
    private void markNotNew() {
        isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
