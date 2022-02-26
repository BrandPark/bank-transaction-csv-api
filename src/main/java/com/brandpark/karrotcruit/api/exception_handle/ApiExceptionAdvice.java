package com.brandpark.karrotcruit.api.exception_handle;

import com.brandpark.karrotcruit.api.upload.exception.CsvColumnNotValidException;
import com.brandpark.karrotcruit.api.upload.exception.IllegalFileFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionAdvice {

    @ExceptionHandler(CsvColumnNotValidException.class)
    public ResponseEntity<ApiError> handleException(CsvColumnNotValidException ex) {

        log.error("API Error : {}", ex.getMessage());

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);

        return createResponseEntity(apiError);
    }

    @ExceptionHandler(IllegalFileFormatException.class)
    public ResponseEntity<ApiError> handleException(IllegalFileFormatException ex) {

        log.error("API Error : {}", ex.getMessage());

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);

        return createResponseEntity(apiError);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleException(MethodArgumentTypeMismatchException ex) {

        log.error("API Error : {}", ex.getMessage());

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getCause());

        return createResponseEntity(apiError);
    }


    private ResponseEntity<ApiError> createResponseEntity(ApiError apiError) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(apiError);
    }
}
