package com.brandpark.karrotcruit.api.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class UploadApiController {

    private final UploadService uploadService;

    @PostMapping("/persist-transaction-list-csv")
    public ResponseEntity persistTransactionListUsingCsv(@RequestParam("file") MultipartFile file) {

        if (!isCsvFile(file)) {
            throw new IllegalFileFormatException("파일이 csv 타입이 아닙니다.");
        }

        long persistedRows = uploadService.persistTransactionListUsingCsv(file);

        return new ResponseEntity(persistedRows, HttpStatus.OK);
    }

    private boolean isCsvFile(MultipartFile file) {
        return file.getContentType().equals("text/csv");
    }
}
