package com.brandpark.karrotcruit.api.bank_transaction;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankCode;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BankCodePersistConverter implements AttributeConverter<BankCode, String> {

    @Override
    public String convertToDatabaseColumn(BankCode attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Converter exception : BankCode가 null입니다.");
        }

        return attribute.getCode();
    }

    @Override
    public BankCode convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isBlank()) {
            throw new IllegalArgumentException("Converter exception : dbData가 비어있습니다.");
        }

        return BankCode.ofCode(dbData);
    }
}
