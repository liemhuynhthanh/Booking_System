package com.huynhliem.controller;

import com.huynhliem.dto.request.UserCreationRequest;
import com.huynhliem.dto.request.UserLoginRequest;
import com.huynhliem.dto.response.BaseResponse;
import com.huynhliem.dto.response.TokenResponse;
import com.huynhliem.dto.response.UserResponse;
import com.huynhliem.service.AuthenticationService;
import com.huynhliem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * POST /auth/signup
     * Đăng ký tài khoản mới, lưu vào database với password đã mã hóa.
     */
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<UserResponse>> signup(@Valid @RequestBody UserCreationRequest request) {
        UserResponse created = userService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.<UserResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("User registered successfully")
                        .data(created)
                        .build());
    }

    /**
     * POST /auth/login
     * Đăng nhập, trả về access token + refresh token.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody UserLoginRequest request) {
        TokenResponse tokenResponse = authenticationService.authenticate(request);
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authenticationService.logout(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authenticationService.refresh(request));
    }
}
