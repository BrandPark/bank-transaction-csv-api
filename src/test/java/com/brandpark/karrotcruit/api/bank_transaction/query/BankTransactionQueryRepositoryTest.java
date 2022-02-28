package com.brandpark.karrotcruit.api.bank_transaction.query;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankCode;
import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransactionRepository;
import com.brandpark.karrotcruit.api.bank_transaction.domain.TransactionType;
import com.brandpark.karrotcruit.api.bank_transaction.dto.PageResult;
import com.brandpark.karrotcruit.util.AssertUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Transactional
@DataJpaTest
class BankTransactionQueryRepositoryTest {

    @Autowired EntityManager entityManager;
    @Autowired BankTransactionRepository bankTransactionRepository;
    BankTransactionQueryRepository bankTransactionQueryRepository;
    int totalElements;
    final int PAGE_0 = 0;
    final int PAGE_SIZE_10 = 10;

    @BeforeEach
    public void setUp() {
        bankTransactionQueryRepository = new BankTransactionQueryRepository(entityManager);
        /*
         * <1월 1일>
         *   - 국민은행(004)에 입금 2번, 출금 1번
         *   - 농협은행(011)은행에 입금 2번, 출금 1번
         * <1월 2일>
         *   - 국민은행(004)은행에 입금 2번, 출금 1번
         *   - 농협은행(011)은행에 입금 2번, 출금 1번
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

    @DisplayName("유저별 거래내역 엔티티 조회 - 거래일자(유저ID ASC")
    @Test
    public void FindAllBankTransactionEntityByUser_UseTransactionDate_OrderBy_UserIdASC() throws Exception {

        // given
        LocalDate expectedTransactionDate = LocalDate.of(2022, 1, 1);   // 2022-01-01

        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 6;
        int expectedContentsSize = expectedTotalElements;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByUser(expectedTransactionDate, null, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);

            assertThat(entity.getYear()).isEqualTo(expectedTransactionDate.getYear());
            assertThat(entity.getMonth()).isEqualTo(expectedTransactionDate.getMonthValue());
            assertThat(entity.getDay()).isEqualTo(expectedTransactionDate.getDayOfMonth());
            assertThat(entity.getTransactionDate()).isEqualTo(expectedTransactionDate);
        }

        assertOrderByUserIdAsc(contents);
    }

    @DisplayName("유저별 거래내역 엔티티 조회 - 거래타입({거래일자, 유저ID} ASC)")
    @Test
    public void FindAllBankTransactionEntityByUser_UseTransactionType_OrderBy_TransactionDateASCAndUserIdASC() throws Exception {

        // given
        TransactionType expectedTransactionType = TransactionType.WITHDRAW;

        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 4;
        int expectedContentsSize = expectedTotalElements;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByUser(null, expectedTransactionType, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);
            assertThat(entity.getTransactionType()).isEqualTo(expectedTransactionType);
        }

        assertOrderByTransactionDateAscAndUserIdAsc(contents);
    }

    @DisplayName("유저별 거래내역 엔티티 조회 - 거래일자, 거래타입(유저ID ASC)")
    @Test
    public void FindAllBankTransactionEntityByUser_UseTransactionDate_And_TransactionType_OrderBy_UserIdASC() throws Exception {

        // given
        LocalDate expectedTransactionDate = LocalDate.of(2022, 1, 2);
        TransactionType expectedTransactionType = TransactionType.WITHDRAW;

        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 2;
        int expectedContentsSize = expectedTotalElements;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByUser(expectedTransactionDate, expectedTransactionType, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);

            assertThat(entity.getYear()).isEqualTo(expectedTransactionDate.getYear());
            assertThat(entity.getMonth()).isEqualTo(expectedTransactionDate.getMonthValue());
            assertThat(entity.getDay()).isEqualTo(expectedTransactionDate.getDayOfMonth());
            assertThat(entity.getTransactionDate()).isEqualTo(expectedTransactionDate);

            assertThat(entity.getTransactionType()).isEqualTo(expectedTransactionType);
        }

        assertOrderByUserIdAsc(contents);
    }

    @DisplayName("유저별 거래내역 엔티티 조회 - 아무 조건 없이({거래일자, 유저ID} ASC)")
    @Test
    public void FindAllBankTransactionEntityByUser_NotCondition_OrderBy_TransactionDateASCAndUserIdASC() throws Exception {

        // given
        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 12;
        int expectedContentsSize = PAGE_SIZE_10;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByUser(null, null, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);
        }

        assertOrderByTransactionDateAscAndUserIdAsc(contents);
    }

    @DisplayName("은행별 거래내역 엔티티 조회 - 거래일자(은행코드 ASC)")
    @Test
    public void FindAllBankTransactionEntityByBank_UseTransactionDate_OrderBy_BankCodeASC() throws Exception {

        // given
        LocalDate expectedTransactionDate = LocalDate.of(2022, 1, 1);   // 2022-01-01

        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 6;
        int expectedContentsSize = expectedTotalElements;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByBank(expectedTransactionDate, null, null, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);

            assertThat(entity.getYear()).isEqualTo(expectedTransactionDate.getYear());
            assertThat(entity.getMonth()).isEqualTo(expectedTransactionDate.getMonthValue());
            assertThat(entity.getDay()).isEqualTo(expectedTransactionDate.getDayOfMonth());
            assertThat(entity.getTransactionDate()).isEqualTo(expectedTransactionDate);
        }

        assertOrderByBankCodeAsc(contents);
    }

    @DisplayName("은행별 거래내역 엔티티 조회 - 거래타입({거래일자 ASC, 은행코드 ASC}")
    @Test
    public void FindAllBankTransactionEntityByBank_UseTransactionType_OrderBy_TransactionDateASCAndBankCodeASC() throws Exception {

        // given
        TransactionType expectedTransactionType = TransactionType.WITHDRAW;

        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 4;
        int expectedContentsSize = expectedTotalElements;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByBank(null, expectedTransactionType, null, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);
            assertThat(entity.getTransactionType()).isEqualTo(expectedTransactionType);
        }

        assertOrderByTransactionDateAscAndBankCodeAsc(contents);
    }

    @DisplayName("은행별 거래내역 엔티티 조회 - 은행코드(거래일자 ASC")
    @Test
    public void FindAllBankTransactionEntityByBank_UseBankCode_OrderBy_TransactionDateASC() throws Exception {

        // given
        BankCode expectedBankCode = BankCode.KB;

        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 6;
        int expectedContentsSize = expectedTotalElements;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByBank(null, null, expectedBankCode, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);

            assertThat(entity.getBankCode()).isEqualTo(expectedBankCode);
        }

        assertOrderByTransactionDateAsc(contents);
    }

    @DisplayName("유저별 거래내역 엔티티 조회 - 거래일자, 거래타입, 은행코드")
    @Test
    public void FindAllBankTransactionEntityByBank_UseTransactionDate_And_TransactionType_And_BankCode() throws Exception {

        // given
        LocalDate expectedTransactionDate = LocalDate.of(2022, 1, 1);
        TransactionType expectedTransactionType = TransactionType.WITHDRAW;
        BankCode expectedBankCode = BankCode.KB;

        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 1;
        int expectedContentsSize = expectedTotalElements;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByBank(expectedTransactionDate, expectedTransactionType, expectedBankCode, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);

            assertThat(entity.getYear()).isEqualTo(expectedTransactionDate.getYear());
            assertThat(entity.getMonth()).isEqualTo(expectedTransactionDate.getMonthValue());
            assertThat(entity.getDay()).isEqualTo(expectedTransactionDate.getDayOfMonth());
            assertThat(entity.getTransactionDate()).isEqualTo(expectedTransactionDate);

            assertThat(entity.getTransactionType()).isEqualTo(expectedTransactionType);

            assertThat(entity.getBankCode()).isEqualTo(expectedBankCode);
        }
    }

    @DisplayName("유저별 거래내역 엔티티 조회 - 아무 조건 없이(거래일자 ASC, 은행코드 ASC)")
    @Test
    public void FindAllBankTransactionEntityByBank_NotCondition_OrderBy_TransactionDateASCAndBankCodeASC() throws Exception {

        // given
        final Pageable pageable = PageRequest.of(PAGE_0, PAGE_SIZE_10);

        int expectedTotalElements = 12;
        int expectedContentsSize = PAGE_SIZE_10;

        // when
        PageResult<BankTransaction> result = bankTransactionQueryRepository.findAllBankTransactionByBank(null, null, null, pageable);

        // then
        AssertUtil.assertPageResult(PAGE_0, PAGE_SIZE_10, expectedTotalElements, result);

        List<BankTransaction> contents = result.getContents();

        assertThat(contents).hasSize(expectedContentsSize);
        for (BankTransaction entity : contents) {
            AssertUtil.assertObjPropertyNotNull(entity);
        }

        assertOrderByTransactionDateAscAndBankCodeAsc(contents);
    }

    private void assertOrderByTransactionDateAsc(List<BankTransaction> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getTransactionDate()).isBeforeOrEqualTo(contents.get(i + 1).getTransactionDate());
        }
    }

    private void assertOrderByUserIdAsc(List<BankTransaction> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getUserId()).isLessThan(contents.get(i + 1).getUserId());
        }
    }

    private void assertOrderByBankCodeAsc(List<BankTransaction> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getBankCode().getCode()).isLessThanOrEqualTo(contents.get(i + 1).getBankCode().getCode());
        }
    }

    private void assertOrderByTransactionDateAscAndUserIdAsc(List<BankTransaction> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getTransactionDate()).isBeforeOrEqualTo(contents.get(i + 1).getTransactionDate());

            if (contents.get(i).getTransactionDate().equals(contents.get(i + 1).getTransactionDate())) {
                assertThat(contents.get(i).getUserId()).isLessThan(contents.get(i + 1).getUserId());
            }
        }
    }

    private void assertOrderByTransactionDateAscAndBankCodeAsc(List<BankTransaction> contents) {
        for (int i = 0; i < contents.size() - 1; i++) {
            assertThat(contents.get(i).getTransactionDate()).isBeforeOrEqualTo(contents.get(i + 1).getTransactionDate());

            if (contents.get(i).getTransactionDate().equals(contents.get(i + 1).getTransactionDate())) {
                assertThat(contents.get(i).getBankCode().getCode()).isLessThanOrEqualTo(contents.get(i + 1).getBankCode().getCode());
            }
        }
    }
}