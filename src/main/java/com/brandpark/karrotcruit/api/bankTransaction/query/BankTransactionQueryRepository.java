package com.brandpark.karrotcruit.api.bankTransaction.query;

import com.brandpark.karrotcruit.api.bankTransaction.domain.TransactionType;
import com.brandpark.karrotcruit.api.bankTransaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bankTransaction.dto.PageResult;
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

        StringBuilder where = new StringBuilder("WHERE 1=1");

        if (transactionDate != null) {
            where.append(" AND bt.transactionDate = :transactionDate");
        }

        if (transactionType != null) {
            where.append(" AND bt.transactionType = :transactionType");
        }

        var pageContentQuery = getPageContentsQuery(pageable, where.toString());
        var totalElementsQuery = getCountElements(where.toString());

        if (transactionDate != null) {
            pageContentQuery.setParameter("transactionDate", transactionDate);
            totalElementsQuery.setParameter("transactionDate", transactionDate);
        }

        if (transactionType != null) {
            pageContentQuery.setParameter("transactionType", transactionType);
            totalElementsQuery.setParameter("transactionType", transactionType);
        }

        List<BankTransaction> contents = pageContentQuery.getResultList();
        long totalElements = totalElementsQuery.getSingleResult();

        return PageResult.create(contents, pageable, totalElements);
    }

    private TypedQuery<BankTransaction> getPageContentsQuery(Pageable pageable, String where) {

        return entityManager.createQuery(
                        "SELECT bt FROM BankTransaction bt " + where
                        , BankTransaction.class)
                .setMaxResults(pageable.getPageSize())
                .setFirstResult((int) pageable.getOffset());
    }

    private TypedQuery<Long> getCountElements(String where) {
        return entityManager.createQuery("SELECT COUNT(bt.id) FROM BankTransaction bt " + where, Long.class);
    }
}
