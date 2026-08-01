package com.huynhliem.exception;

/**
 * HTTP 400 - Bad Request
 * Ném ra khi dữ liệu đầu vào từ client không hợp lệ về mặt logic nghiệp vụ.
 * Ví dụ: password và confirmPassword không khớp, email sai định dạng,
 * thiếu trường bắt buộc mà không qua @Valid annotation.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
