package com.huynhliem.exception;

/**
 * HTTP 401 - Unauthorized
 * Ném ra khi token JWT không hợp lệ (sai chữ ký, sai loại token, bị giả mạo).
 * Khác với TokenExpiredException ở chỗ: token chưa hết hạn nhưng không pass validation.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
