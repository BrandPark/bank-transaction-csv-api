package com.brandpark.karrotcruit.api.bankTransaction.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
}
