package com.brandpark.karrotcruit.api.upload.exception;

import lombok.Getter;

@Getter
public class CsvColumnNotValidException extends RuntimeException{

    private final long row;

    public CsvColumnNotValidException(String message, long row) {
        super(message);
        this.row = row;
    }

    public CsvColumnNotValidException(String message, Throwable cause, long row) {
        super(message, cause);
        this.row = row;
    }
}
