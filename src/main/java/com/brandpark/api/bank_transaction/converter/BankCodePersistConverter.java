package com.brandpark.api.bank_transaction.converter;

import com.brandpark.api.bank_transaction.domain.BankCode;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Slf4j
@Converter
public class BankCodePersistConverter implements AttributeConverter<BankCode, String> {

    @Override
    public String convertToDatabaseColumn(BankCode attribute) {
        if (attribute == null) {
            log.error("Converter exception : {}", "BankCode가 null입니다.");
            throw new IllegalArgumentException("BankCode가 null입니다.");
        }

        return attribute.getCode();
    }

    @Override
    public BankCode convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isBlank()) {
            log.error("Converter exception : {}", "테이블 레코드의 은행코드 컬럼이 비어있습니다.");
            throw new IllegalArgumentException("dbData가 비어있습니다.");
        }

        return BankCode.ofCode(dbData);
    }
}
