package com.brandpark.karrotcruit.api.bankTransaction;

import com.brandpark.karrotcruit.api.bankTransaction.domain.BankTransaction;
import com.brandpark.karrotcruit.api.bankTransaction.domain.TransactionType;
import com.brandpark.karrotcruit.api.bankTransaction.dto.BankTransactionResponse;
import com.brandpark.karrotcruit.api.bankTransaction.dto.PageResult;
import com.brandpark.karrotcruit.api.bankTransaction.query.BankTransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class BankTransactionApiController {

    private final BankTransactionQueryRepository bankTransactionQueryRepository;

    @GetMapping("/bank-transactions/by-user")
    public PageResult<BankTransactionResponse> getAllTransactionListByUser(
            @RequestParam(value = "transaction_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate transactionDate
            , @RequestParam(value = "transaction_type", required = false) TransactionType transactionType
            , Pageable pageable) {

        PageResult<BankTransaction> byUser = bankTransactionQueryRepository.findAllBankTransactionByUser(transactionDate, transactionType, pageable);

        return convertToResponsePageResult(byUser);
    }

    private PageResult<BankTransactionResponse> convertToResponsePageResult(PageResult<BankTransaction> page) {

        List<BankTransaction> entityContents = page.getContents();

        List<BankTransactionResponse> responseContents = entityContents.stream()
                .map(BankTransactionResponse::new)
                .collect(Collectors.toList());

        return PageResult.create(responseContents, page.getPageable(), page.getTotalElements());
    }
}
