package com.huynhliem.exception;

import com.huynhliem.dto.response.BaseResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global Exception Handler — bắt tất cả exception trong toàn bộ ứng dụng.
 * Mọi handler đều trả về {@link BaseResponse} để đảm bảo cấu trúc JSON nhất quán.
 *
 * <p>Quy ước HTTP status code:</p>
 * <ul>
 *   <li>400 Bad Request       : Dữ liệu đầu vào sai / không hợp lệ về nghiệp vụ</li>
 *   <li>401 Unauthorized      : Chưa xác thực hoặc token không hợp lệ / hết hạn</li>
 *   <li>403 Forbidden         : Đã xác thực nhưng không có quyền truy cập</li>
 *   <li>404 Not Found         : Tài nguyên không tồn tại</li>
 *   <li>409 Conflict          : Tài nguyên đã tồn tại (trùng lặp)</li>
 *   <li>415 Unsupported Media : Content-Type request không được hỗ trợ</li>
 *   <li>500 Internal Error    : Lỗi không xác định phía server</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleValidationException(MethodArgumentNotValidException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();
    }
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<?> handleInvalidRequest(InvalidRequestException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();
    }
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse<?> handleBadCredentials(BadCredentialsException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid username or password")
                .build();
    }
    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse<?> handleTokenExpired(TokenExpiredException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .build();
    }
    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse<?> handleInvalidToken(InvalidTokenException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .build();
    }
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse<?> handleJwtException(JwtException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("Token is invalid or expired")
                .build();
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseResponse<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .build();
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseResponse<?> handleUsernameNotFound(UsernameNotFoundException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .build();
    }
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public BaseResponse<?> handleDuplicateResource(DuplicateResourceException ex) {
        return BaseResponse.builder()
                .code(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .build();
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<?> handleGenericException(Exception ex) {
        return BaseResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.")
                .build();
    }
}