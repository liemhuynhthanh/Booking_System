package com.huynhliem.service;

import com.huynhliem.dto.request.UserLoginRequest;
import com.huynhliem.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

import javax.naming.NameNotFoundException;

public interface AuthenticationService {
    TokenResponse authenticate(UserLoginRequest request);
    TokenResponse refresh(HttpServletRequest request);
    String logout(HttpServletRequest request);
}
