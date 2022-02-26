package com.brandpark.karrotcruit.api.upload;

import com.brandpark.karrotcruit.util.AssertUtil;
import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransactionRepository;
import com.brandpark.karrotcruit.api.upload.exception.CsvColumnNotValidException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class UploadRepositoryTest {

    @Autowired EntityManager entityManager;
    @Autowired BankTransactionRepository bankTransactionRepository;
    UploadRepository uploadRepository = null;

    @BeforeEach
    public void setUp() {
        uploadRepository = new UploadRepository(entityManager);
    }

    @DisplayName("csv 파일로부터 엔티티 저장 - 실패(비어있는 컬럼이 있는 경우)")
    @Test
    public void BankTransactionPersistFromCsvFile_Fail_When_ExistsBlankColumn() throws Exception {

        // given
        String invalidRecord = "1,2021,1,1,4,,29000,DEPOSIT";   // 은행코드가 비어있다.

        MockMultipartFile csvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", invalidRecord.getBytes(StandardCharsets.UTF_8));

        // when, then
        Assertions.assertThrows(CsvColumnNotValidException.class, () -> {
            uploadRepository.batchInsertBankTransactionFromCsvFile(csvFile);
        });
    }

    @DisplayName("csv 파일로부터 엔티티 저장 - 실패(은행 코드를 찾을 수 없을 때)")
    @Test
    public void BankTransactionPersistFromCsvFile_Fail_When_IllegalBankCode() throws Exception {

        // given
        String invalidRecord = "1,2021,1,1,4,111,29000,DEPOSIT";   // 잘못된 은행 코드 111

        MockMultipartFile csvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", invalidRecord.getBytes(StandardCharsets.UTF_8));

        // when
        Assertions.assertThrows(CsvColumnNotValidException.class, () -> {
            uploadRepository.batchInsertBankTransactionFromCsvFile(csvFile);
        });
    }

    @DisplayName("csv 파일로부터 엔티티 저장 - 실패(잘못된 형식의 컬럼이 있는 경우)")
    @Test
    public void BankTransactionPersistFromCsvFile_Fail_When_IllegalTypeColumn() throws Exception {

        // given
        String invalidRecord = "1,2021,1,1,4,004,29000원,DEPOSIT";   // 금액에 문자열이 들어있는 경우

        MockMultipartFile notCsvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", invalidRecord.getBytes(StandardCharsets.UTF_8));

        // when
        Assertions.assertThrows(CsvColumnNotValidException.class, () -> {
            uploadRepository.batchInsertBankTransactionFromCsvFile(notCsvFile);
        });
    }

    @DisplayName("csv 파일로부터 엔티티 저장 - 성공")
    @Test
    public void BankTransactionPersistFromCsvFile_Success() throws Exception {

        // given
        String record1 = "1,2021,1,1,4,004,29000,DEPOSIT";
        String record2 = "2,2021,1,1,5,020,88000,DEPOSIT";
        String totalRecord = record1 + "\n" + record2;
        int csvRowCount = 2;

        MockMultipartFile csvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", totalRecord.getBytes(StandardCharsets.UTF_8));

        // when
        uploadRepository.batchInsertBankTransactionFromCsvFile(csvFile);
        entityManager.flush();
        entityManager.clear();

        // then
        List<BankTransaction> all = bankTransactionRepository.findAll();

        assertThat(all).hasSize(csvRowCount);

        BankTransaction actual = all.get(0);
        String[] expectedCols = record1.split(",");

        AssertUtil.assertBankTransaction(actual, expectedCols);
    }
}