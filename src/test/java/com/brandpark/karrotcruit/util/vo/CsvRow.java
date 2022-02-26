package com.brandpark.karrotcruit.util.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor @NoArgsConstructor
@Data
public class CsvRow {
    private String id;
    private String year;
    private String month;
    private String day;
    private String userId;
    private String bankCode;
    private String transactionAmount;
    private String transactionType;

    public CsvRow(String[] csvCols) {
        id = csvCols[0];
        year = csvCols[1];
        month = csvCols[2];
        day = csvCols[3];
        userId = csvCols[4];
        bankCode = csvCols[5];
        transactionAmount = csvCols[6];
        transactionType = csvCols[7];
    }

    public static CsvRow createCsvRowDefault() {
        return CsvRow.builder()
                .id("1")
                .year("2022")
                .month("1")
                .day("1")
                .userId("1")
                .bankCode("004")
                .transactionAmount("12000")
                .transactionType("WITHDRAW")
                .build();
    }

    public void setTransactionDate(String year, String month, String day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s", id, year, month, day, userId, bankCode, transactionAmount, transactionType);
    }
}
