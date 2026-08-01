package com.huynhliem.exception;

/**
 * HTTP 404 - Not Found
 * Ném ra khi không tìm thấy tài nguyên trong hệ thống.
 * Ví dụ: user không tồn tại, email không tìm thấy, token không có trong DB.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
