package com.huynhliem.service.impl;

import com.huynhliem.dto.request.UserLoginRequest;
import com.huynhliem.dto.response.TokenResponse;
import com.huynhliem.exception.TokenExpiredException;
import com.huynhliem.model.Token;
import com.huynhliem.model.User;
import com.huynhliem.repository.UserRepository;
import com.huynhliem.service.AuthenticationService;
import com.huynhliem.service.TokenService;
import com.huynhliem.utils.JwtUtils;
import com.huynhliem.utils.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final UserDetailService userDetailService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    public TokenResponse authenticate(UserLoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getName(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findUserByName(request.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getName()));

        UserDetails userDetails = userDetailService.loadUserByUsername(request.getName());

        // Tạo access token
        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        tokenService.save(Token.builder()
                .username(user.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .usedId(user.getId())
                .build();
    }

    @Override
    public TokenResponse refresh(HttpServletRequest request) {
        final String token = request.getHeader("x-token");
        log.info("Received refresh token request with token: {}", token);
        if (StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("Token is missing");
        }

        // Kiểm tra refresh token còn hạn không — nếu hết hạn yêu cầu đăng nhập lại
        try {
            jwtUtils.isTokenValid(token, TokenType.REFRESH_TOKEN);
        } catch (JwtException ex) {
            if (ex.getMessage().contains("expired")) {
                throw new TokenExpiredException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
            }
            throw new TokenExpiredException("Refresh token không hợp lệ. Vui lòng đăng nhập lại.");
        }

        final String username = jwtUtils.extractUsername(token, TokenType.REFRESH_TOKEN);
        log.info("Refreshing token for user: {}", username);

        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        UserDetails userDetails = userDetailService.loadUserByUsername(username);

        String newAccessToken = jwtUtils.generateToken(userDetails);
        // Giữ lại refresh token cũ, không tạo mới — token cũ vẫn còn hạn
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .usedId(user.getId())
                .build();
    }

    @Override
    public String logout(HttpServletRequest request) {
        final String token = request.getHeader("x-token");
        if (StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("Token is missing");
        }

        final String username= jwtUtils.extractUsername(token, TokenType.ACCESS_TOKEN);
    Token currentToken=tokenService.getByUserName(username);
    tokenService.delete(currentToken);

        return "Logged out successfully";
    }
}
