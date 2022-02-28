package com.brandpark.karrotcruit.api.bank_transaction;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankCode;
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

    final int PAGE_0 = 0;
    final int PAGE_SIZE_10 = 10;
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
                "1,2022,1,1,3,011,29000,DEPOSIT", "2,2022,1,1,2,004,29000,DEPOSIT", "3,2022,1,1,1,011,29000,WITHDRAW"
                , "4,2022,1,1,6,004,29000,DEPOSIT", "5,2022,1,1,5,011,29000,DEPOSIT", "6,2022,1,1,4,004,29000,WITHDRAW"
                , "7,2022,1,2,9,004,29000,DEPOSIT", "8,2022,1,2,8,011,29000,DEPOSIT", "9,2022,1,2,7,004,29000,WITHDRAW"
                , "10,2022,1,2,12,011,29000,DEPOSIT", "11,2022,1,2,11,004,29000,DEPOSIT", "12,2022,1,2,10,011,29000,WITHDRAW"
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

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("transaction_date", invalidTransactionDate)
                        .param("transaction_type", transactionType)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);

                    ApiError error = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @DisplayName("유저별 입출금 내역 조회 - 실패(거래타입이 올바르지 않은 경우)")
    @Test
    public void RetrieveTransactionByUser_Fail_When_InvalidRequest_TransactionType() throws Exception {

        // given
        String transactionDate = "2022-01-01";
        String invalidTransactionType = "WITH";    // 거래타입이 올바르지 않은 경우

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("transaction_date", transactionDate)
                        .param("transaction_type", invalidTransactionType)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);

                    ApiError error = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @DisplayName("유저별 입출금 내역 조회 - 성공(거래일자로 조회: 유저ID ASC)")
    @Test
    public void RetrieveTransactionByUser_Success_When_UseTransactionDate_OrderBy_UserIdASC() throws Exception {

        // given
        String transactionDateParam = "2022-01-01";

        int expectedTotalElements = 6;
        int expectedContentsSize = expectedTotalElements;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("transaction_date", transactionDateParam)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getTransactionDate()).isEqualTo(transactionDateParam);
                    }

                    assertOrderByUserIdAsc(contents);
                });
    }

    @DisplayName("유저별 입출금 내역 조회 - 성공(거래타입으로 조회: {거래일자, 유저ID} ASC)")
    @Test
    public void RetrieveTransactionByUser_Success_When_UseTransactionType_OrderBy_TransactionDateASCAndUserIdASC() throws Exception {

        // given
        String transactionTypeParam = "DEPOSIT";

        int expectedTotalElements = 8;
        int expectedContentsSize = expectedTotalElements;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("transaction_type", transactionTypeParam)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getTransactionType()).isEqualTo(transactionTypeParam);
                    }

                    assertOrderByTransactionDateAscAndUserIdAsc(contents);
                });
    }

    @DisplayName("유저별 입출금 내역 조회 - 성공(모든 조건을 사용하여 조회 : 유저ID ASC)")
    @Test
    public void RetrieveTransactionByUser_Success_When_UseAllCondition_OrderBy_UserIdASC() throws Exception {

        // given
        String transactionDateParam = "2022-01-02";
        String transactionTypeParam = "WITHDRAW";

        int expectedTotalElements = 2;
        int expectedContentsSize = expectedTotalElements;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10))
                        .param("transaction_date", transactionDateParam)
                        .param("transaction_type", transactionTypeParam))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getTransactionDate()).isEqualTo(transactionDateParam);
                        assertThat(content.getTransactionType()).isEqualTo(transactionTypeParam);
                    }

                    assertOrderByUserIdAsc(contents);
                });
    }

    @DisplayName("유저별 입출금 내역 조회 - 성공(조건 없이 조회: {거래일자 ASC, 유저ID ASC})")
    @Test
    public void RetrieveTransactionByUser_Success_When_NotUseCondition_OrderBy_TrnasactionDateASCAndUserIDAsc() throws Exception {

        // given
        int expectedTotalElements = 12;
        int expectedContentsSize = PAGE_SIZE_10;

        assertThat(expectedTotalElements).isEqualTo(totalElements);

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-user")
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                    }

                    assertOrderByTransactionDateAscAndUserIdAsc(contents);
                });
    }

    @DisplayName("은행별 입출금 내역 조회 - 실패(거래일자 형식이 잘못된 경우)")
    @Test
    public void RetrieveTransactionByBank_Fail_When_InvalidRequest_TransactionDate() throws Exception {

        // given
        String invalidTransactionDate = "2022-01-1";    // yyyy-MM-dd 를 지키지 않는 형식
        String transactionType = "WITHDRAW";
        String bankCode = BankCode.KB.getCode();

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("transaction_date", invalidTransactionDate)
                        .param("transaction_type", transactionType)
                        .param("bank_code", bankCode)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);

                    ApiError error = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @DisplayName("은행별 입출금 내역 조회 - 실패(거래타입이 올바르지 않은 경우)")
    @Test
    public void RetrieveTransactionByBank_Fail_When_InvalidRequest_TransactionType() throws Exception {

        // given
        String transactionDate = "2022-01-01";
        String invalidTransactionType = "입금";    // 거래타입이 올바르지 않은 경우
        String bankCode = BankCode.KB.getCode();

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("transaction_date", transactionDate)
                        .param("transaction_type", invalidTransactionType)
                        .param("bank_code", bankCode)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);

                    ApiError error = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ApiError.class);

                    assertThat(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @DisplayName("은행별 입출금 내역 조회 - 성공(거래일자로 조회 : 은행코드 ASC)")
    @Test
    public void RetrieveTransactionByBank_Success_When_UseTransactionDate_OrderBy_BankCodeASC() throws Exception {

        // given
        String transactionDateParam = "2022-01-01";

        int expectedTotalElements = 6;
        int expectedContentsSize = expectedTotalElements;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("transaction_date", transactionDateParam)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getTransactionDate()).isEqualTo(transactionDateParam);
                    }

                    assertOrderByBankCodeAsc(contents);
                });
    }

    @DisplayName("은행별 입출금 내역 조회 - 성공(거래타입으로 조회 : {거래일자 ASC, 은행코드 ASC})")
    @Test
    public void RetrieveTransactionByBank_Success_When_UseTransactionType_OrderBy_TransactionDateASCAndBankCodeASC() throws Exception {

        // given
        String transactionTypeParam = "DEPOSIT";

        int expectedTotalElements = 8;
        int expectedContentsSize = expectedTotalElements;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("transaction_type", transactionTypeParam)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getTransactionType()).isEqualTo(transactionTypeParam);
                    }

                    for (int i = 0; i < contents.size() - 1; i++) {
                        assertThat(contents.get(i).getTransactionDate()).isLessThanOrEqualTo(contents.get(i + 1).getTransactionDate());

                        if (contents.get(i).getTransactionDate().equals(contents.get(i + 1).getTransactionDate())) {
                            assertThat(contents.get(i).getBankCode()).isLessThanOrEqualTo(contents.get(i + 1).getBankCode());
                        }
                    }
                });
    }

    @DisplayName("은행별 입출금 내역 조회 - 성공(은행코드로 조회 : 거래일자 ASC)")
    @Test
    public void RetrieveTransactionByBank_Success_When_UseBankCode_OrderByTransactionDateASC() throws Exception {

        // given
        String bankCodeParam = "004";

        int expectedTotalElements = 6;
        int expectedContentsSize = expectedTotalElements;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("bank_code", bankCodeParam)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getBankCode()).isEqualTo(bankCodeParam);
                    }

                    assertOrderByTransactionDateASC(contents);
                });
    }

    @DisplayName("은행별 입출금 내역 조회 - 성공(모든 조건을 사용하여 조회)")
    @Test
    public void RetrieveTransactionByBank_Success_When_UseAllCondition() throws Exception {

        // given
        String transactionType = "DEPOSIT";
        String transactionDate = "2022-01-02";
        String bankCodeParam = "004";

        int expectedTotalElements = 2;
        int expectedContentsSize = expectedTotalElements;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("bank_code", bankCodeParam)
                        .param("transaction_date", transactionDate)
                        .param("transaction_type", transactionType)
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getBankCode()).isEqualTo(bankCodeParam);
                        assertThat(content.getTransactionType()).isEqualTo(transactionType);
                        assertThat(content.getTransactionDate()).isEqualTo(transactionDate);
                    }
                });
    }

    @DisplayName("은행별 입출금 내역 조회 - 성공(아무 조건도 사용하지 않을 경우 : {거래일자 ASC, 은행코드 ASC})")
    @Test
    public void RetrieveTransactionByBank_Success_When_NotUseCondition_OrderBy_TransactionDateASCAndBankCodeASC() throws Exception {

        // given
        int expectedTotalElements = 12;
        int expectedContentsSize = PAGE_SIZE_10;

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("page", String.valueOf(PAGE_0))
                        .param("size", String.valueOf(PAGE_SIZE_10)))
                .andExpect(status().isOk())
                .andExpect(result -> {

                    String responseJson = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

                    PageResult<BankTransactionResponse> responsePage = objectMapper.readValue(responseJson, new TypeReference<>() {
                    });

                    // 페이지 정보 테스트
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // 페이지 내용물 테스트
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                    }

                    assertOrderByTransactionDateAscAndBankCodeAsc(contents);
                });
    }

    private void assertOrderByUserIdAsc(List<BankTransactionResponse> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getUserId()).isLessThanOrEqualTo(contents.get(i + 1).getUserId());
        }
    }

    private void assertOrderByTransactionDateAscAndUserIdAsc(List<BankTransactionResponse> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getTransactionDate()).isLessThanOrEqualTo(contents.get(i + 1).getTransactionDate());

            if (contents.get(i).getTransactionDate().equals(contents.get(i + 1).getTransactionDate())) {
                assertThat(contents.get(i).getUserId()).isLessThan(contents.get(i + 1).getUserId());
            }
        }
    }

    private void assertOrderByBankCodeAsc(List<BankTransactionResponse> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getBankCode()).isLessThanOrEqualTo(contents.get(i + 1).getBankCode());
        }
    }

    private void assertOrderByTransactionDateASC(List<BankTransactionResponse> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getTransactionDate()).isLessThanOrEqualTo(contents.get(i + 1).getTransactionDate());
        }
    }

    private void assertOrderByTransactionDateAscAndBankCodeAsc(List<BankTransactionResponse> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getTransactionDate()).isLessThanOrEqualTo(contents.get(i + 1).getTransactionDate());

            if (contents.get(i).getTransactionDate().equals(contents.get(i + 1).getTransactionDate())) {
                assertThat(contents.get(i).getBankCode()).isLessThanOrEqualTo(contents.get(i + 1).getBankCode());
            }
        }
    }
}