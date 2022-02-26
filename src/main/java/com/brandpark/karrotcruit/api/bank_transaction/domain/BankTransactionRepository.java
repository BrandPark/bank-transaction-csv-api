package com.brandpark.karrotcruit.api.bank_transaction.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
}
