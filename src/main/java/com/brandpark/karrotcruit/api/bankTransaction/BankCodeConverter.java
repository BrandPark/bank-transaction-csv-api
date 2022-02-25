package com.brandpark.karrotcruit.api.bankTransaction;

import com.brandpark.karrotcruit.api.bankTransaction.domain.BankCode;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BankCodeConverter implements AttributeConverter<BankCode, String> {

    @Override
    public String convertToDatabaseColumn(BankCode attribute) {
        return attribute.getCode();
    }

    @Override
    public BankCode convertToEntityAttribute(String dbData) {
        return BankCode.of(dbData);
    }
}
