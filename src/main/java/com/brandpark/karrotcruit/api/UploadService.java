package com.brandpark.karrotcruit.api;

import com.brandpark.karrotcruit.api.dto.BankTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UploadService {

    private final BankTransactionRepository bankTransactionRepository;

    @Transactional
    public int uploadFile(MultipartFile file) {
        if (!file.getContentType().equals("text/csv")) {
            throw new IllegalStateException("파일이 csv 타입이 아닙니다.");
        }

        List<BankTransaction> transactionList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {

            String line = null;

            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");

                if (split.length != 8) {
                    throw new IllegalStateException("데이터의 컬럼 형식이 올바르지 않습니다.");
                }

                transactionList.add(BankTransaction.createBankTransaction(split));
            }
        } catch (UnsupportedEncodingException e) {
            log.error("거래내역 저장 중 에러 발생 : {}", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("거래내역 저장 중 에러 발생 : {}", e.getMessage());
            e.printStackTrace();
        }

        return bankTransactionRepository.saveAll(transactionList).size();
    }
}
