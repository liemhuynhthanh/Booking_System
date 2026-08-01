package com.huynhliem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BaseResponse <T>{
    @Builder.Default
    private int code = 1000;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}
