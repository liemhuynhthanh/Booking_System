package com.huynhliem.exception;

/**
 * HTTP 409 - Conflict
 * Ném ra khi tài nguyên đã tồn tại trong hệ thống và không thể tạo trùng lặp.
 * Ví dụ: đăng ký tài khoản với username hoặc email đã được sử dụng.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
