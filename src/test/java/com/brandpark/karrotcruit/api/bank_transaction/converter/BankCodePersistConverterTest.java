package com.brandpark.karrotcruit.api.bank_transaction.converter;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankCode;
import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bank_transaction.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class BankCodePersistConverterTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager entityManager;

    @DisplayName("BankCode enum이 code로 변환되어 db에 저장이 된다.")
    @Test
    public void ConvertEnumToCode_When_persistToDb() throws Exception {

        // given
        BankCode codeEnum = BankCode.KB;

        BankTransaction entity = BankTransaction.builder()
                .id(1L).year(2022).month(1).day(1).transactionAmount(1000).transactionType(TransactionType.WITHDRAW)
                .transactionDate(LocalDate.of(2022, 1, 1)).userId(4L)
                .bankCode(codeEnum).build();

        // when
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        // then
        String expectedColumnValue = "004";

        List<BankTransaction> result = entityManager.createNativeQuery(
                        "SELECT * FROM BANK_TRANSACTION WHERE BANK_CODE = :code"
                        , BankTransaction.class)
                .setParameter("code", expectedColumnValue)
                .getResultList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(entity.getId());
    }

    @DisplayName("엔티티 조회 시 code가 BankCode enum으로 변환된다.")
    @Test
    public void ConvertCodeToEnum_When_RetrieveEntity() throws Exception {

        // given
        String code = "004";
        BankCode expectedEnum = BankCode.ofCode(code);

        BankTransaction entity = BankTransaction.builder()
                .id(1L).year(2022).month(1).day(1).transactionAmount(1000).transactionType(TransactionType.WITHDRAW)
                .transactionDate(LocalDate.of(2022, 1, 1)).userId(4L)
                .bankCode(expectedEnum).build();

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        // when
        List<BankTransaction> result = entityManager.createNativeQuery(
                        "SELECT * FROM BANK_TRANSACTION WHERE BANK_CODE = :code"
                        , BankTransaction.class)
                .setParameter("code", code)
                .getResultList();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(entity.getId());
        assertThat(result.get(0).getBankCode()).isEqualTo(expectedEnum);
    }
}