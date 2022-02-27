package com.brandpark.karrotcruit.util;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bank_transaction.dto.BankTransactionResponse;
import com.brandpark.karrotcruit.api.bank_transaction.dto.PageResult;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
public abstract class AssertUtil {
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

    public static void assertPageResult(int page, int pageSize, long totalElements, PageResult actual) {

        int expectedTotalPages = totalElements == 0 ? 0 : (int) ((totalElements - 1) / pageSize) + 1;
        int expectedOffset = page * pageSize;
        int expectedContentsSize = (totalElements - expectedOffset) >= pageSize ? pageSize : (int) (totalElements - expectedOffset);

        assertThat(actual.getPageNumber()).isEqualTo(page);
        assertThat(actual.getPageSize()).isEqualTo(pageSize);
        assertThat(actual.getOffset()).isEqualTo(expectedOffset);
        assertThat(actual.getTotalPages()).isEqualTo(expectedTotalPages);
        assertThat(actual.getTotalElements()).isEqualTo(totalElements);
        assertThat(actual.getContentsSize()).isEqualTo(expectedContentsSize);
    }

    public static void assertObjPropertyNotNull(BankTransactionResponse obj) {
        assertThat(obj.getBankTransactionId()).isNotNull();
        assertThat(obj.getTransactionDate()).isNotNull();
        assertThat(obj.getBankCode()).isNotNull();
        assertThat(obj.getUserId()).isNotNull();
        assertThat(obj.getTransactionAmount()).isNotNull();
        assertThat(obj.getTransactionType()).isNotNull();
    }

    public static void assertObjPropertyNotNull(BankTransaction obj) {
        assertThat(obj.getId()).isNotNull();
        assertThat(obj.getYear()).isNotNull();
        assertThat(obj.getDay()).isNotNull();
        assertThat(obj.getMonth()).isNotNull();
        assertThat(obj.getTransactionDate()).isNotNull();
        assertThat(obj.getUserId()).isNotNull();
        assertThat(obj.getBankCode()).isNotNull();
        assertThat(obj.getTransactionAmount()).isNotNull();
        assertThat(obj.getTransactionType()).isNotNull();
    }
}
