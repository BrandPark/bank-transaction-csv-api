package com.brandpark.api.bank_transaction;

import com.brandpark.api.bank_transaction.domain.BankCode;
import com.brandpark.api.bank_transaction.domain.BankTransaction;
import com.brandpark.api.bank_transaction.domain.BankTransactionRepository;
import com.brandpark.api.bank_transaction.dto.BankTransactionResponse;
import com.brandpark.api.bank_transaction.dto.PageResult;
import com.brandpark.api.exception_handle.ApiError;
import com.brandpark.util.AssertUtil;
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
         * <1??? 1???>
         *   - '004'????????? ?????? 2???, ?????? 1???
         *   - '011'????????? ?????? 2???, ?????? 1???
         * <1??? 2???>
         *   - '004'????????? ?????? 2???, ?????? 1???
         *   - '011'????????? ?????? 2???, ?????? 1???
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

    @DisplayName("????????? ????????? ?????? ?????? - ??????(???????????? ????????? ????????? ??????)")
    @Test
    public void RetrieveTransactionByUser_Fail_When_InvalidRequest_TransactionDate() throws Exception {

        // given
        String invalidTransactionDate = "2022-01-1";    // yyyy-MM-dd ??? ????????? ?????? ??????
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

    @DisplayName("????????? ????????? ?????? ?????? - ??????(??????????????? ???????????? ?????? ??????)")
    @Test
    public void RetrieveTransactionByUser_Fail_When_InvalidRequest_TransactionType() throws Exception {

        // given
        String transactionDate = "2022-01-01";
        String invalidTransactionType = "WITH";    // ??????????????? ???????????? ?????? ??????

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

    @DisplayName("????????? ????????? ?????? ?????? - ??????(??????????????? ??????: ??????ID ASC)")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getTransactionDate()).isEqualTo(transactionDateParam);
                    }

                    assertOrderByUserIdAsc(contents);
                });
    }

    @DisplayName("????????? ????????? ?????? ?????? - ??????(?????????????????? ??????: {????????????, ??????ID} ASC)")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getTransactionType()).isEqualTo(transactionTypeParam);
                    }

                    assertOrderByTransactionDateAscAndUserIdAsc(contents);
                });
    }

    @DisplayName("????????? ????????? ?????? ?????? - ??????(?????? ????????? ???????????? ?????? : ??????ID ASC)")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
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

    @DisplayName("????????? ????????? ?????? ?????? - ??????(?????? ?????? ??????: {???????????? ASC, ??????ID ASC})")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                    }

                    assertOrderByTransactionDateAscAndUserIdAsc(contents);
                });
    }

    @DisplayName("????????? ????????? ?????? ?????? - ??????(???????????? ????????? ????????? ??????)")
    @Test
    public void RetrieveTransactionByBank_Fail_When_InvalidRequest_TransactionDate() throws Exception {

        // given
        String invalidTransactionDate = "2022-01-1";    // yyyy-MM-dd ??? ????????? ?????? ??????
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

    @DisplayName("????????? ????????? ?????? ?????? - ??????(??????????????? ???????????? ?????? ??????)")
    @Test
    public void RetrieveTransactionByBank_Fail_When_InvalidRequest_TransactionType() throws Exception {

        // given
        String transactionDate = "2022-01-01";
        String invalidTransactionType = "??????";    // ??????????????? ???????????? ?????? ??????
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

    @DisplayName("????????? ????????? ?????? ?????? - ??????(??????????????? ?????? : ???????????? ASC)")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getTransactionDate()).isEqualTo(transactionDateParam);
                    }

                    assertOrderByBankCodeAsc(contents);
                });
    }

    @DisplayName("????????? ????????? ?????? ?????? - ??????(?????????????????? ?????? : {???????????? ASC, ???????????? ASC})")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
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

    @DisplayName("????????? ????????? ?????? ?????? - ??????(??????????????? ?????? : ???????????? ASC)")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
                    List<BankTransactionResponse> contents = responsePage.getContents();

                    assertThat(contents).hasSize(expectedContentsSize);

                    for (BankTransactionResponse content : contents) {
                        AssertUtil.assertObjPropertyNotNull(content);
                        assertThat(content.getBankCode()).isEqualTo(bankCodeParam);
                    }

                    assertOrderByTransactionDateASC(contents);
                });
    }

    @DisplayName("????????? ????????? ?????? ?????? - ??????(?????? ????????? ???????????? ??????)")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
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

    @DisplayName("????????? ????????? ?????? ?????? - ??????(?????? ????????? ???????????? ?????? ?????? : {???????????? ASC, ???????????? ASC})")
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

                    // ????????? ?????? ?????????
                    AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, responsePage);

                    // ????????? ????????? ?????????
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