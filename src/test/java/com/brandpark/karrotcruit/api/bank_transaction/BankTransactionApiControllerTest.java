package com.brandpark.karrotcruit.api.bank_transaction;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransactionRepository;
import com.brandpark.karrotcruit.api.bank_transaction.dto.BankTransactionResponse;
import com.brandpark.karrotcruit.api.bank_transaction.dto.PageResult;
import com.brandpark.karrotcruit.api.exception_handle.ApiError;
import com.brandpark.karrotcruit.util.AssertUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class BankTransactionApiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BankTransactionRepository bankTransactionRepository;
    @Autowired EntityManager entityManager;

    int totalElements;

    @BeforeEach
    public void setUp() {
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        /*
         * <1월 1일>
         *   - '004'은행에 입금 2번, 출금 1번
         *   - '011'은행에 입금 2번, 출금 1번
         * <1월 2일>
         *   - '004'은행에 입금 2번, 출금 1번
         *   - '011'은행에 입금 2번, 출금 1번
         */
        String[] csvRows = {
                "1,2022,1,1,4,004,29000,DEPOSIT", "2,2022,1,1,4,004,29000,DEPOSIT", "3,2022,1,1,4,004,29000,WITHDRAW"
                , "4,2022,1,1,4,011,29000,DEPOSIT", "5,2022,1,1,4,011,29000,DEPOSIT", "6,2022,1,1,4,011,29000,WITHDRAW"
                , "7,2022,1,2,4,004,29000,DEPOSIT", "8,2022,1,2,4,004,29000,DEPOSIT", "9,2022,1,2,4,004,29000,WITHDRAW"
                , "10,2022,1,2,4,011,29000,DEPOSIT", "11,2022,1,2,4,011,29000,DEPOSIT", "12,2022,1,2,4,011,29000,WITHDRAW"
        };

        totalElements = csvRows.length;

        for (int i = 0; i < csvRows.length; i++) {
            entityManager.persist(BankTransaction.csvRowToEntity(csvRows[i].split(",")));
        }

        entityManager.flush();
        entityManager.clear();

        List<BankTransaction> saved = bankTransactionRepository.findAll();
        assertThat(saved).hasSize(totalElements);
    }

    @DisplayName("유저별 입출금 내역 조회 - 실패(거래일자 형식이 잘못된 경우)")
    @Test
    public void RetrieveTransactionByUser_Fail_When_InvalidRequest_TransactionDate() throws Exception {

        // given
        String invalidTransactionDate = "2022-01-1";    // yyyy-MM-dd 를 지키지 않는 형식
        String transactionType = "WITHDRAW";
        String pageParam = "0";
        String pageSizeParam = "10";

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("transaction_date", invalidTransactionDate)
                        .param("transaction_type", transactionType)
                        .param("page", pageParam)
                        .param("size", pageSizeParam))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);

                    ApiError error = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @DisplayName("유저별 입출금 내역 조회 - 실패(거래 타입이 올바르지 않은 경우)")
    @Test
    public void RetrieveTransactionByUser_Fail_When_InvalidRequest_TransactionType() throws Exception {

        // given
        String transactionDate = "2022-01-01";
        String invalidTransactionType = "WITH";    // 거래타입이 올바르지 않은 경우
        String pageParam = "0";
        String pageSizeParam = "10";

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("transaction_date", transactionDate)
                        .param("transaction_type", invalidTransactionType)
                        .param("page", pageParam)
                        .param("size", pageSizeParam))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);

                    ApiError error = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @DisplayName("유저별 입출금 내역 조회 - 성공(거래일자로 조회)")
    @Test
    public void RetrieveTransactionByUser_Success_When_UseTransactionDate() throws Exception {

        // given
        String transactionDateParam = "2022-01-01";
        int pageParam = 0;
        int pageSizeParam = 10;

        int expectedTotalElements = 6;
        int expectedContentsSize = 6;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("transaction_date", transactionDateParam)
                        .param("page", String.valueOf(pageParam))
                        .param("size", String.valueOf(pageSizeParam)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    AssertUtil.assertPageResult(pageParam, pageSizeParam, expectedTotalElements, responsePage);

                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        assertThat(content.getTransactionDate()).isEqualTo(transactionDateParam);
                    }
                });
    }
}