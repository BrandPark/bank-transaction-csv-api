package com.brandpark.api.bank_transaction.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

@NoArgsConstructor
@Data
public class PageResult<T> {

    @JsonIgnore
    private Pageable pageable;
    private int pageNumber;
    private int pageSize;
    private long offset;
    private int totalPages;
    private long totalElements;
    private int contentsSize;
    private List<T> contents;

    public static <T> PageResult<T> create(List<T> contents, Pageable pageable, long totalElements) {
        PageResult<T> ret = new PageResult<>();

        ret.contents = contents;
        ret.pageNumber = pageable.getPageNumber();
        ret.pageSize = pageable.getPageSize();
        ret.offset = pageable.getOffset();
        ret.pageable = pageable;
        ret.contentsSize = contents.size();
        ret.totalElements = totalElements;
        ret.totalPages = totalElements == 0 ? 0 : (int) ((totalElements - 1) / pageable.getPageSize()) + 1;

        return ret;
    }
}
