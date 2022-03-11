package com.brandpark.api.bank_transaction.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class BankCodeRequestConverterTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    
    @DisplayName("은행 코드를 Enum 으로 변환 - 실패(존재하지 않는 은행 코드)")
    @Test
    public void ConvertCodeToEnum_Fail_When_InvalidCode() throws Exception {
    
        // given
        String invalidCode = "999";

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("bank_code", invalidCode))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);
                });
    }

    @DisplayName("은행 코드를 Enum 으로 변환 - 성공")
    @Test
    public void ConvertCodeToEnum_Success() throws Exception {

        // given
        String code = "004";    // 국민은행 코드드

        // when, then
        mockMvc.perform(get("/api/v1/bank-transactions/by-bank")
                        .param("bank_code", code))
                .andExpect(status().isOk());
    }
}