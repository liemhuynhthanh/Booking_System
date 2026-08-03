package com.huynhliem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    @Builder.Default
    private int code = 1000;
    private String message;
    private T data;
    private Integer currentPage;
    private Integer pageSize;
    private Long totalItems;
    private Integer totalPages;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
