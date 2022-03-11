package com.brandpark.api.upload;

import com.brandpark.api.bank_transaction.domain.BankTransaction;
import com.brandpark.api.upload.exception.CsvColumnNotValidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Repository
public class UploadRepository {

    @Value("${batchSize}")
    private int batchSize = 1;

    private final EntityManager entityManager;

    @Transactional
    public long batchInsertBankTransactionFromCsvFile(MultipartFile file) {

        List<BankTransaction> batchInsertBuff = new ArrayList<>();
        int count = 0;

        long totalInsertedRow = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line = null;
            long rowNum = 0;

            while ((line = br.readLine()) != null) {
                rowNum++;

                String[] row = line.split(",");

                BankTransaction transactionEntity = csvRowConvertToBankTransaction(row, rowNum);

                batchInsertBuff.add(transactionEntity);

                if(++count % batchSize == 0) {
                    totalInsertedRow += batchInsertBuff.size();

                    flushBuff(batchInsertBuff);
                }
            }

            if(!batchInsertBuff.isEmpty()) {
                totalInsertedRow += batchInsertBuff.size();

                flushBuff(batchInsertBuff);
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

    private void flushBuff(List<BankTransaction> batchInsertBuff) {

        for (BankTransaction entity : batchInsertBuff) {
            entityManager.persist(entity);
        }

        entityManager.flush();
        entityManager.clear();
        batchInsertBuff.clear();
    }
}

