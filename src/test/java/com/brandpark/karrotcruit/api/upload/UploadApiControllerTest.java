package com.brandpark.karrotcruit.api.upload;

import com.brandpark.karrotcruit.AssertUtil;
import com.brandpark.karrotcruit.api.bankTransaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bankTransaction.domain.BankTransactionRepository;
import com.brandpark.karrotcruit.api.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class UploadApiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired BankTransactionRepository bankTransactionRepository;
    @Autowired ObjectMapper objectMapper;

    @DisplayName("csv 파일로부터 거래내역 저장 - 실패(csv 파일이 아닌 경우)")
    @Test
    public void BankTransactionPersistFromCsvFile_Fail_When_NotCsvFile() throws Exception {

        // given
        String record = "1,2021,1,1,4,004,29000,DEPOSIT";

        MockMultipartFile notCsvFile
                = new MockMultipartFile("file", "transaction.txt", MediaType.TEXT_PLAIN_VALUE, record.getBytes(StandardCharsets.UTF_8));

        // when
        mockMvc.perform(multipart("/api/v1/persist-transaction-list-csv")
                        .file(notCsvFile))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {

                    assertThat(result.getResolvedException()).isInstanceOf(IllegalFileFormatException.class);

                    ApiError errorResponse = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(errorResponse.getMessage()).isNotBlank();
                });
    }

    @DisplayName("csv 파일로부터 거래내역 저장 - 실패(비어있는 컬럼이 있는 경우)")
    @Test
    public void BankTransactionPersistFromCsvFile_Fail_When_ExistsBlankColumn() throws Exception {

        // given
        String invalidRecord = "1,2021,1,1,4,,29000,DEPOSIT";   // 은행코드가 비어있다.

        MockMultipartFile csvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", invalidRecord.getBytes(StandardCharsets.UTF_8));

        // when
        mockMvc.perform(multipart("/api/v1/persist-transaction-list-csv")
                        .file(csvFile))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {

                    assertThat(result.getResolvedException()).isInstanceOf(CsvColumnNotValidException.class);

                    ApiError errorResponse = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(errorResponse.getMessage()).isNotBlank();
                });
    }

    @DisplayName("csv 파일로부터 거래내역 저장 - 실패(은행 코드를 찾을 수 없을 때)")
    @Test
    public void BankTransactionPersistFromCsvFile_Fail_When_IllegalBankCode() throws Exception {

        // given
        String invalidRecord = "1,2021,1,1,4,111,29000,DEPOSIT";   // 잘못된 은행 코드 111

        MockMultipartFile csvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", invalidRecord.getBytes(StandardCharsets.UTF_8));

        // when
        mockMvc.perform(multipart("/api/v1/persist-transaction-list-csv")
                        .file(csvFile))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {

                    assertThat(result.getResolvedException()).isInstanceOf(CsvColumnNotValidException.class);

                    ApiError errorResponse = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(errorResponse.getMessage()).isNotBlank();
                });
    }

    @DisplayName("csv 파일로부터 거래내역 저장 - 실패(잘못된 형식의 컬럼이 있는 경우)")
    @Test
    public void BankTransactionPersistFromCsvFile_Fail_When_IllegalTypeColumn() throws Exception {

        // given
        String invalidRecord = "1,2021,1,1,4,004,29000원,DEPOSIT";   // 금액에 문자열이 들어있는 경우

        MockMultipartFile notCsvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", invalidRecord.getBytes(StandardCharsets.UTF_8));

        // when
        mockMvc.perform(multipart("/api/v1/persist-transaction-list-csv")
                        .file(notCsvFile))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {

                    assertThat(result.getResolvedException()).isInstanceOf(CsvColumnNotValidException.class);

                    ApiError errorResponse = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(errorResponse.getMessage()).isNotBlank();
                });
    }

    @DisplayName("csv 파일로부터 거래내역 저장 - 성공")
    @Test
    public void BankTransactionPersistFromCsvFile_Success() throws Exception {

        // given
        String record1 = "1,2021,1,1,4,004,29000,DEPOSIT";
        String record2 = "2,2021,1,1,5,020,88000,DEPOSIT";
        String totalRecord = record1 + "\n" + record2;
        int csvRowCount = 2;

        MockMultipartFile notCsvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", totalRecord.getBytes(StandardCharsets.UTF_8));

        // when
        mockMvc.perform(multipart("/api/v1/persist-transaction-list-csv")
                        .file(notCsvFile))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    Long persistedRowCnt = objectMapper.readValue(json, Long.class);

                    assertThat(persistedRowCnt).isEqualTo(csvRowCount);
                });

        // then
        List<BankTransaction> all = bankTransactionRepository.findAll();

        assertThat(all.size()).isEqualTo(csvRowCount);

        BankTransaction actual = all.get(0);
        String[] expectedCols = record1.split(",");

        AssertUtil.assertBankTransaction(actual, expectedCols);
    }
}