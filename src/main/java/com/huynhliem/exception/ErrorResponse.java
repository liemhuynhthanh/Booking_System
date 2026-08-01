package com.huynhliem.exception;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ErrorResponse {
    private int code;
    private String message;
    private Date timestamp;
    private String path;
    private String error;
}
