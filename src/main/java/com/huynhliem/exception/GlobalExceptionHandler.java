package com.huynhliem.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

/**
 * Global Exception Handler — bắt tất cả exception trong toàn bộ ứng dụng.
 * Mỗi handler trả về ErrorResponse với HTTP status code tương ứng.
 *
 * Quy ước HTTP status code:
 *  - 400 Bad Request       : Dữ liệu đầu vào sai / không hợp lệ về nghiệp vụ
 *  - 401 Unauthorized      : Chưa xác thực hoặc token không hợp lệ / hết hạn
 *  - 403 Forbidden         : Đã xác thực nhưng không có quyền truy cập
 *  - 404 Not Found         : Tài nguyên không tồn tại
 *  - 409 Conflict          : Tài nguyên đã tồn tại (trùng lặp)
 *  - 415 Unsupported Media : Content-Type request không được hỗ trợ
 *  - 500 Internal Error    : Lỗi không xác định phía server
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────
    // 400 - BAD REQUEST
    // ─────────────────────────────────────────────

    /**
     * Bắt lỗi validation từ @Valid trên @RequestBody.
     * Ví dụ: trường bắt buộc bị thiếu, email sai định dạng, độ dài tối thiểu...
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getMessage();
        // Lấy thông báo lỗi ngắn gọn từ BindingResult (bỏ stacktrace dài)
        int start = message.lastIndexOf("[");
        int end = message.lastIndexOf("]");
        if (start >= 0 && end > start) {
            message = message.substring(start + 1, end - 1);
        }
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Bắt lỗi nghiệp vụ: dữ liệu không hợp lệ do logic (vd: password != confirmPassword).
     */
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequest(InvalidRequestException ex, WebRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // ─────────────────────────────────────────────
    // 401 - UNAUTHORIZED
    // ─────────────────────────────────────────────

    /**
     * Bắt lỗi xác thực sai: username hoặc password không đúng khi đăng nhập.
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid username or password", request);
    }

    /**
     * Bắt lỗi token hết hạn (access token hoặc refresh token đã quá thời hạn).
     * Client cần refresh token hoặc đăng nhập lại.
     */
    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleTokenExpired(TokenExpiredException ex, WebRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * Bắt lỗi token không hợp lệ (sai chữ ký, sai loại token, bị giả mạo).
     */
    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidToken(InvalidTokenException ex, WebRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * Bắt lỗi JWT tổng quát từ thư viện io.jsonwebtoken (parse thất bại).
     */
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleJwtException(JwtException ex, WebRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "Token is invalid or expired", request);
    }

    // ─────────────────────────────────────────────
    // 404 - NOT FOUND
    // ─────────────────────────────────────────────

    /**
     * Bắt lỗi không tìm thấy tài nguyên: user, token, email, booking...
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Bắt lỗi Spring Security khi không tìm thấy user trong UserDetailsService.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUsernameNotFound(UsernameNotFoundException ex, WebRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ─────────────────────────────────────────────
    // 409 - CONFLICT
    // ─────────────────────────────────────────────

    /**
     * Bắt lỗi trùng lặp tài nguyên: username hoặc email đã được đăng ký.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateResource(DuplicateResourceException ex, WebRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ─────────────────────────────────────────────
    // 415 - UNSUPPORTED MEDIA TYPE
    // ─────────────────────────────────────────────

    /**
     * Bắt lỗi khi client gửi Content-Type không đúng.
     * Ví dụ: gửi "text/plain" thay vì "application/json".
     * Thường xảy ra khi dùng Postman chọn Body → Text thay vì JSON.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        return buildError(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type không được hỗ trợ. Vui lòng dùng 'application/json'",
                request
        );
    }

    // ─────────────────────────────────────────────
    // 500 - INTERNAL SERVER ERROR
    // ─────────────────────────────────────────────

    /**
     * Bắt tất cả exception chưa được xử lý cụ thể ở trên.
     * Không expose chi tiết lỗi ra ngoài để tránh lộ thông tin hệ thống.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex, WebRequest request) {
        // Log lỗi thật sự để debug, nhưng chỉ trả về thông báo chung cho client
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.", request);
    }

    // ─────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────

    /**
     * Tạo ErrorResponse chuẩn từ HttpStatus, message và WebRequest.
     */
    private ErrorResponse buildError(HttpStatus status, String message, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(new Date())
                .code(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false)) // vd: "uri=/auth/login"
                .build();
    }
}