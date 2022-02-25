package com.brandpark.karrotcruit.api.bankTransaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankCode {

    KB("004", "국민은행"),
    NH("011", "농협은행"),
    WB("020", "우리은행"),
    SH("088", "신한은행"),
    KK("090", "카카오뱅크");

    private final String code;
    private final String bankName;

    public static BankCode of(String code) {
        for (BankCode bc : BankCode.values()) {
            if(bc.getCode().equals(code)) {
                return bc;
            }
        }

        throw new IllegalArgumentException("코드와 일치하는 은행이 존재하지 않습니다.");
    }
}
