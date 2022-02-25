package com.brandpark.karrotcruit.api.upload;

import com.brandpark.karrotcruit.api.bankTransaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bankTransaction.domain.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UploadRepository {

    @Value("${batchSize}")
    private int batchSize;

    private final BankTransactionRepository bankTransactionRepository;

    public long batchInsertBankTransactionFromCsvFile(MultipartFile file) {

        List<BankTransaction> batchInsertBuff = new ArrayList<>();
        int count = batchSize;

        long totalInsertedRow = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line = null;
            long rowNum = 0;

            while ((line = br.readLine()) != null) {
                rowNum++;

                String[] row = line.split(",");

                BankTransaction transactionEntity = csvRowConvertToBankTransaction(row, rowNum);

                batchInsertBuff.add(transactionEntity);
                count--;

                if(count <= 0) {
                    totalInsertedRow += batchInsertBuff.size();
                    count = batchSize;

                    buffFlush(batchInsertBuff);
                }
            }

            if(!batchInsertBuff.isEmpty()) {
                totalInsertedRow += batchInsertBuff.size();

                buffFlush(batchInsertBuff);
            }

        } catch(IOException e) {
            e.printStackTrace();
            log.error("파일을 읽는 중 에러 발생 : {}", e.getMessage());
        }

        return totalInsertedRow;
    }

    private BankTransaction csvRowConvertToBankTransaction(String[] row, long rowNum) {
        if(row.length != 8) {
            throw new CsvColumnNotValidException(rowNum + "행의 컬럼의 수가 일치하지 않습니다.", rowNum);
        }

        try {
            return BankTransaction.csvRowToEntity(row);
        } catch(NumberFormatException e) {
            throw new CsvColumnNotValidException(rowNum + "행의 컬럼 중 타입이 올바르지 않은 것이 있습니다.", e, rowNum);
        } catch(IllegalArgumentException e) {
            throw new CsvColumnNotValidException(rowNum + "행의 컬럼 중 타입이 올바르지 않은 것이 있습니다.", e, rowNum);
        }
    }

    private void buffFlush(List<BankTransaction> batchInsertBuff) {
        bankTransactionRepository.saveAll(batchInsertBuff);
        batchInsertBuff.clear();
    }
}

