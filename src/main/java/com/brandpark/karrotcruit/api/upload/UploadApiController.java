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

        validateRequest(file);

        long persistedRows = uploadService.persistTransactionListUsingCsv(file);

        return new ResponseEntity(persistedRows, HttpStatus.OK);
    }

    private void validateRequest(MultipartFile file) {
        if (file.getContentType() == null || !file.getContentType().equals("text/csv")) {
            throw new IllegalFileFormatException("지원되지 않는 파일형식입니다. csv 파일만 가능합니다.");
        }
    }
}
