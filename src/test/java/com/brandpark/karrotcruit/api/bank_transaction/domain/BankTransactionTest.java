package com.brandpark.karrotcruit.api.bank_transaction.domain;

import com.brandpark.karrotcruit.util.AssertUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BankTransactionTest {

    @DisplayName("문자열로된 컬럼 배열로 BankTransaction 엔티티를 생성 - 실패(컬럼의 타입이 유효하지 않을 경우)")
    @Test
    public void CreateEntity_From_StringColumnArrays_Fail_When_InvalidTypeColumn() throws Exception {

        // given
        String[] cols = "1,2021,1,1,4,004,29000원,DEPOSIT".split(",");    // 거래액에 문자가 포함

        // when, then
        Assertions.assertThrows(NumberFormatException.class, () -> {
            BankTransaction.csvRowToEntity(cols);
        });
    }

    @DisplayName("문자열로된 컬럼 배열로 BankTransaction 엔티티를 생성 - 실패(은행코드가 존재하지 않는 경우)")
    @Test
    public void CreateEntity_From_StringColumnArrays_Fail_When_NotFoundBankCode() throws Exception {

        // given
        String[] cols = "1,2021,1,1,4,000,29000,DEPOSIT".split(",");    // 존재하지 않는 은행코드(000)

        // when, then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BankTransaction.csvRowToEntity(cols);
        });
    }

    @DisplayName("문자열로된 컬럼 배열로 BankTransaction 엔티티를 생성 - 실패(존재하지 않는 거래타입인 경우)")
    @Test
    public void CreateEntity_From_StringColumnArrays_Fail_When_NotFoundTransactionType() throws Exception {

        // given
        String[] cols = "1,2021,1,1,4,004,29000,입금".split(",");    // 존재하지 않는 거래타입(입금)

        // when, then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BankTransaction.csvRowToEntity(cols);
        });
    }

    @DisplayName("문자열로된 컬럼 배열로 BankTransaction 엔티티를 생성 - 실패(컬럼의 개수가 8개가 아닌 경우)")
    @Test
    public void CreateEntity_From_StringColumnArrays_Fail_When_InvalidColumnCount() throws Exception {

        // given
        String[] cols = "1,2021,1,1,4,004,29000".split(",");    // 7개의 컬럼(거래타입이 빠졌다.)
        assertThat(cols).hasSize(7);

        // when, then
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BankTransaction.csvRowToEntity(cols);
        });
    }

    @DisplayName("문자열로된 컬럼 배열로 BankTransaction 엔티티를 생성 - 성공")
    @Test
    public void CreateEntity_From_StringColumnArrays_Success() throws Exception {

        // given
        String[] cols = "1,2021,1,1,4,004,29000,DEPOSIT".split(",");

        // when
        BankTransaction result = BankTransaction.csvRowToEntity(cols);

        // then
        AssertUtil.assertBankTransaction(result, cols);
    }
}
