package com.huynhliem.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseError {
    private int code;
    private String message;
}
