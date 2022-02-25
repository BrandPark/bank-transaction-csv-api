package com.brandpark.karrotcruit.api.upload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class UploadService {

    private final UploadRepository uploadRepository;

    @Transactional
    public long persistTransactionListUsingCsv(MultipartFile file) {
        return uploadRepository.batchInsertBankTransactionFromCsvFile(file);
    }


}
