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