package com.huynhliem.exception;

/**
 * HTTP 401 - Unauthorized
 * Ném ra khi token JWT hết hạn (access token hoặc refresh token).
 * Client phải dùng refresh token để lấy access token mới,
 * hoặc đăng nhập lại nếu refresh token cũng đã hết hạn.
 */
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
