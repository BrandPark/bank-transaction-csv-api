package com.brandpark.karrotcruit.api;

import com.brandpark.karrotcruit.api.dto.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
}
