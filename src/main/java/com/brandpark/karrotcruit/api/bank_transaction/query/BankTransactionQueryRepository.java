package com.brandpark.karrotcruit.api.bank_transaction.query;

import com.brandpark.karrotcruit.api.bank_transaction.domain.BankCode;
import com.brandpark.karrotcruit.api.bank_transaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bank_transaction.domain.TransactionType;
import com.brandpark.karrotcruit.api.bank_transaction.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Repository
public class BankTransactionQueryRepository {

    private final EntityManager entityManager;

    public PageResult<BankTransaction> findAllBankTransactionByUser(LocalDate transactionDate, TransactionType transactionType, Pageable pageable) {

        final StringBuilder where = new StringBuilder(" WHERE 1=1");

        if (transactionDate != null) {
            where.append(" AND bt.transactionDate = :transactionDate");
        }

        if (transactionType != null) {
            where.append(" AND bt.transactionType = :transactionType");
        }

        var contentsQuery = getContentsQuery(pageable, where.toString());
        var totalElementsQuery = getCountElements(where.toString());

        if (transactionDate != null) {
            contentsQuery.setParameter("transactionDate", transactionDate);
            totalElementsQuery.setParameter("transactionDate", transactionDate);
        }

        if (transactionType != null) {
            contentsQuery.setParameter("transactionType", transactionType);
            totalElementsQuery.setParameter("transactionType", transactionType);
        }

        List<BankTransaction> contents = contentsQuery.getResultList();
        final long totalElements = totalElementsQuery.getSingleResult();

        return PageResult.create(contents, pageable, totalElements);
    }

    public PageResult<BankTransaction> findAllBankTransactionByBank(LocalDate transactionDate, TransactionType transactionType, BankCode bankCode, Pageable pageable) {

        final StringBuilder where = new StringBuilder(" WHERE 1=1");

        if (transactionDate != null) {
            where.append(" AND bt.transactionDate = :transactionDate");
        }

        if (transactionType != null) {
            where.append(" AND bt.transactionType = :transactionType");
        }

        if (bankCode != null) {
            where.append(" AND bt.bankCode = :bankCode");
        }

        var contentsQuery = getContentsQuery(pageable, where.toString());
        var totalElementsQuery = getCountElements(where.toString());

        if (transactionDate != null) {
            contentsQuery.setParameter("transactionDate", transactionDate);
            totalElementsQuery.setParameter("transactionDate", transactionDate);
        }

        if (transactionType != null) {
            contentsQuery.setParameter("transactionType", transactionType);
            totalElementsQuery.setParameter("transactionType", transactionType);
        }

        if (bankCode != null) {
            contentsQuery.setParameter("bankCode", bankCode);
            totalElementsQuery.setParameter("bankCode", bankCode);
        }

        List<BankTransaction> contents = contentsQuery.getResultList();
        final long totalElements = totalElementsQuery.getSingleResult();

        return PageResult.create(contents, pageable, totalElements);
    }

    private TypedQuery<BankTransaction> getContentsQuery(Pageable pageable, final String where) {

        return entityManager.createQuery(
                        "SELECT bt FROM BankTransaction bt" + where
                        , BankTransaction.class)
                .setMaxResults(pageable.getPageSize())
                .setFirstResult((int) pageable.getOffset());
    }

    private TypedQuery<Long> getCountElements(final String where) {
        return entityManager.createQuery("SELECT COUNT(bt.id) FROM BankTransaction bt " + where, Long.class);
    }
}
