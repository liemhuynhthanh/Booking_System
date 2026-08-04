package com.huynhliem.service;

import com.huynhliem.dto.request.PasswordResetDTO;
import com.huynhliem.dto.request.UserLoginRequest;
import com.huynhliem.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    TokenResponse authenticate(UserLoginRequest request);

    TokenResponse refresh(HttpServletRequest request);

    String logout(HttpServletRequest request);

    String forgotPassword(String email);

    String resetPassword(String secretKey);

    String changePassword(PasswordResetDTO passwordResetDTO);
}
