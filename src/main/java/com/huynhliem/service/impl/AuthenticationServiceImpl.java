package com.huynhliem.service.impl;

import java.time.ZoneId;
import java.time.LocalDateTime;

import com.huynhliem.dto.request.PasswordResetDTO;
import com.huynhliem.dto.request.UserLoginRequest;
import com.huynhliem.dto.response.TokenResponse;
import com.huynhliem.exception.InvalidRequestException;
import com.huynhliem.exception.InvalidTokenException;
import com.huynhliem.exception.ResourceNotFoundException;
import com.huynhliem.exception.TokenExpiredException;
import com.huynhliem.model.Token;
import com.huynhliem.model.User;
import com.huynhliem.repository.UserRepository;
import com.huynhliem.service.AuthenticationService;
import com.huynhliem.service.TokenService;
import com.huynhliem.service.UserService;
import com.huynhliem.utils.JwtUtils;
import com.huynhliem.utils.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getName()));

        UserDetails userDetails = userDetailService.loadUserByUsername(request.getName());

        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        tokenService.save(Token.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiredAt(jwtUtils.extractExpiration(refreshToken, TokenType.REFRESH_TOKEN).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .revoked(false)
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
        log.info("Received refresh token request");

        if (StringUtils.isBlank(token)) {
            throw new InvalidRequestException("Refresh token is missing in header 'x-token'");
        }

        try {
            jwtUtils.isTokenValid(token, TokenType.REFRESH_TOKEN);
        } catch (ExpiredJwtException ex) {

            throw new TokenExpiredException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        } catch (JwtException ex) {

            throw new InvalidTokenException("Refresh token không hợp lệ.");
        }

        final String username = jwtUtils.extractUsername(token, TokenType.REFRESH_TOKEN);
        log.info("Refreshing token for user: {}", username);

        try {
            Token dbToken = tokenService.getByToken(token);
            if (dbToken.isRevoked()) {
                throw new InvalidTokenException("Refresh token is revoked.");
            }
        } catch (Exception e) {
            throw new InvalidTokenException("Refresh token not found or invalid.");
        }

        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserDetails userDetails = userDetailService.loadUserByUsername(username);

        String newAccessToken = jwtUtils.generateToken(userDetails);

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
            throw new InvalidRequestException("Token is missing in header 'x-token'");
        }

        final String username = jwtUtils.extractUsername(token, TokenType.ACCESS_TOKEN);
        
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        tokenService.revokeAllTokensForUser(user.getId());

        return "Logged out successfully";
    }

    @Override
    public String forgotPassword(String email) {

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + email));

        UserDetails userDetails = userDetailService.loadUserByUsername(user.getName());
        String resetToken = jwtUtils.generateResetToken(userDetails);

        String confirm = String.format(
                "curl --location --request POST 'http://localhost:8080/auth/reset-password' \\\n--data '%s'",
                resetToken
        );
        log.info("Reset password command for user {}: {}", user.getName(), confirm);

        return "Reset password email sent successfully";
    }

    @Override
    public String resetPassword(String secretKey) {
        final String username = jwtUtils.extractUsername(secretKey, TokenType.RESET_TOKEN);

        userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!jwtUtils.isTokenValid(secretKey, TokenType.RESET_TOKEN)) {
            throw new InvalidTokenException("Reset token is invalid or expired");
        }

        return "Token is valid. Proceed to change password.";
    }

    @Override
    public String changePassword(PasswordResetDTO passwordResetDTO) {
        final String username = jwtUtils.extractUsername(passwordResetDTO.getSecretKey(), TokenType.RESET_TOKEN);

        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!jwtUtils.isTokenValid(passwordResetDTO.getSecretKey(), TokenType.RESET_TOKEN)) {
            throw new InvalidTokenException("Reset token is invalid or expired");
        }

        if (!passwordResetDTO.getPassword().equals(passwordResetDTO.getConfirmPassword())) {
            throw new InvalidRequestException("Password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(passwordResetDTO.getConfirmPassword()));
        userService.savePassword(user);

        return "Password changed successfully";
    }
}
