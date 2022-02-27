package com.brandpark.karrotcruit.api.bank_transaction.converter;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankCode;
import org.springframework.core.convert.converter.Converter;

public class BankCodeRequestConverter implements Converter<String, BankCode> {
    @Override
    public BankCode convert(String bankCode) {
        return BankCode.ofCode(bankCode);
    }
}
