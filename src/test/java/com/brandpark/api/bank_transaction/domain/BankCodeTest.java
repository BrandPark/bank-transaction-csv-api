package com.brandpark.api.bank_transaction.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BankCodeTest {

    @DisplayName("코드 '004'로 BankCode.KB를 만들 수 있다.")
    @Test
    public void GetEnum_Success_When_UseCode_004() throws Exception {
        BankCode bankCode = BankCode.ofCode("004");

        assertThat(bankCode).isEqualTo(BankCode.KB);
        assertThat(BankCode.KB.getBankName()).isEqualTo("국민은행");
    }

    @DisplayName("코드 '011'로 BankCode.NH를 만들 수 있다.")
    @Test
    public void GetEnum_Success_When_UseCode_011() throws Exception {
        BankCode bankCode = BankCode.ofCode("011");

        assertThat(bankCode).isEqualTo(BankCode.NH);
        assertThat(BankCode.NH.getBankName()).isEqualTo("농협은행");
    }

    @DisplayName("코드 '020'으로 BankCode.WB를 만들 수 있다.")
    @Test
    public void GetEnum_Success_When_UseCode_020() throws Exception {
        BankCode bankCode = BankCode.ofCode("020");

        assertThat(bankCode).isEqualTo(BankCode.WB);
        assertThat(BankCode.WB.getBankName()).isEqualTo("우리은행");
    }

    @DisplayName("코드 '088'로 BankCode.SH를 만들 수 있다.")
    @Test
    public void GetEnum_Success_When_UseCode_088() throws Exception {
        BankCode bankCode = BankCode.ofCode("088");

        assertThat(bankCode).isEqualTo(BankCode.SH);
        assertThat(BankCode.SH.getBankName()).isEqualTo("신한은행");
    }

    @DisplayName("코드 '090'으로 BankCode.KK를 만들 수 있다.")
    @Test
    public void GetEnum_Success_When_UseCode_090() throws Exception {
        BankCode bankCode = BankCode.ofCode("090");

        assertThat(bankCode).isEqualTo(BankCode.KK);
        assertThat(BankCode.KK.getBankName()).isEqualTo("카카오뱅크");
    }

    @DisplayName("존재하지 않는 코드인 경우 예외가 발생한다.")
    @Test
    public void ThrowException_When_InvalidCode() throws Exception {
        String invalidCode = "999";

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BankCode.ofCode(invalidCode);
        });
    }
}