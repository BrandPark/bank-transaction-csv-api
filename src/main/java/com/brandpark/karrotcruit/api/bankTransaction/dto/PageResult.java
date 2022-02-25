package com.brandpark.karrotcruit.api.bankTransaction.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

@NoArgsConstructor
@Data
public class PageResult<T> {

    private List<T> contents;
    @JsonIgnore
    private int pageNumber;
    @JsonIgnore
    private int pageSize;
    @JsonIgnore
    private long offset;
    private Pageable pageable;
    private int totalPages;
    private long totalElements;

    public static <T> PageResult<T> create(List<T> contents, Pageable pageable, long totalElements) {
        PageResult<T> ret = new PageResult<>();

        ret.contents = contents;
        ret.pageNumber = pageable.getPageNumber();
        ret.pageSize = pageable.getPageSize();
        ret.offset = pageable.getOffset();
        ret.pageable = pageable;
        ret.totalElements = totalElements;
        ret.totalPages = (int)(totalElements / pageable.getPageSize());

        return ret;
    }
}
