package com.brandpark.karrotcruit;

import com.brandpark.karrotcruit.api.bankTransaction.domain.BankTransaction;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Component
public class AssertUtil {
    public static void assertBankTransaction(BankTransaction actual, BankTransaction expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getYear()).isEqualTo(expected.getYear());
        assertThat(actual.getMonth()).isEqualTo(expected.getMonth());
        assertThat(actual.getDay()).isEqualTo(expected.getDay());
        assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
        assertThat(actual.getBankCode()).isEqualTo(expected.getBankCode());
        assertThat(actual.getTransactionAmount()).isEqualTo(expected.getTransactionAmount());
        assertThat(actual.getTransactionType()).isEqualTo(expected.getTransactionType());
    }
}
