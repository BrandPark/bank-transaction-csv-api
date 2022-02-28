package com.brandpark.karrotcruit.api.upload;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransactionRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class PerformanceTest {

    @Autowired MockMvc mockMvc;
    @Autowired BankTransactionRepository bankTransactionRepository;
    @Autowired EntityManager entityManager;
    static StringBuilder csvContents;

    @BeforeAll
    public static void beforeAll() {
        csvContents = new StringBuilder();

        for (int i = 1; i <= 100000; i++) {
            csvContents.append(i).append(",2021,1,1,4,004,29000,DEPOSIT\n");
        }
    }

    @DisplayName("Csv파일로부터 거래내역 10만개 저장")
    @Test
    public void SaveBankTransaction_100000_FromCsv() throws Exception {

        // given
        String contents = csvContents.toString();   // 10만개

        MockMultipartFile csvFile
                = new MockMultipartFile("file", "transaction.csv", "text/csv", contents.getBytes(StandardCharsets.UTF_8));

        // when
        mockMvc.perform(multipart("/api/v1/bank-transactions/persist-csv")
                        .file(csvFile))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();
        // then
//        long count = bankTransactionRepository.count();
//        assertThat(count).isEqualTo(100000);
    }
}
