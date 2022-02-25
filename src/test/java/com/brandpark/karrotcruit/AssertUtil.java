package com.brandpark.karrotcruit;

import com.brandpark.karrotcruit.api.bankTransaction.domain.BankTransaction;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Component
public class AssertUtil {
    public static void assertBankTransaction(BankTransaction actual, String[] expectedCols) {
        assertThat(actual.getId()).isEqualTo(Long.parseLong(expectedCols[0]));
        assertThat(actual.getYear()).isEqualTo(Integer.parseInt(expectedCols[1]));
        assertThat(actual.getMonth()).isEqualTo(Integer.parseInt(expectedCols[2]));
        assertThat(actual.getDay()).isEqualTo(Integer.parseInt(expectedCols[3]));
        assertThat(actual.getUserId()).isEqualTo(Long.parseLong(expectedCols[4]));
        assertThat(actual.getBankCode().getCode()).isEqualTo(expectedCols[5]);
        assertThat(actual.getTransactionAmount()).isEqualTo(Long.parseLong(expectedCols[6]));
        assertThat(actual.getTransactionType().name()).isEqualTo(expectedCols[7]);
    }
}
