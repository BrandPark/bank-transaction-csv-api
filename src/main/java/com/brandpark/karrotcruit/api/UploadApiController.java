package com.brandpark.karrotcruit.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@RestController
public class UploadApiController {

    private final UploadService uploadService;

    @PostMapping
    public ResponseEntity uploadData(@RequestParam("file") MultipartFile file) {

        int count = uploadService.uploadFile(file);
        System.out.println(count);

        return new ResponseEntity(HttpStatus.OK);
    }


}
