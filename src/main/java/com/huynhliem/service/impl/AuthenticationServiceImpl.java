package com.huynhliem.service.impl;

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

    /**
     * Đăng nhập — xác thực username/password, trả về access token + refresh token.
     * Ném 401 BadCredentialsException nếu sai thông tin đăng nhập.
     */
    @Override
    public TokenResponse authenticate(UserLoginRequest request) {
        try {
            // Spring Security kiểm tra username + password qua AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getName(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            // 401 - sai username hoặc password
            throw new BadCredentialsException("Invalid username or password");
        }

        // Lấy thông tin user từ DB để lấy ID
        User user = userRepository.findUserByName(request.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getName()));

        UserDetails userDetails = userDetailService.loadUserByUsername(request.getName());

        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Lưu token vào DB để quản lý phiên đăng nhập
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

    /**
     * Làm mới access token bằng refresh token (gửi qua header "x-token").
     * Ném 400 nếu thiếu token.
     * Ném 401 TokenExpiredException nếu refresh token đã hết hạn.
     * Ném 401 InvalidTokenException nếu refresh token không hợp lệ.
     */
    @Override
    public TokenResponse refresh(HttpServletRequest request) {
        final String token = request.getHeader("x-token");
        log.info("Received refresh token request");

        // 400 - thiếu token trong header
        if (StringUtils.isBlank(token)) {
            throw new InvalidRequestException("Refresh token is missing in header 'x-token'");
        }

        // Kiểm tra refresh token: phân biệt hết hạn vs không hợp lệ
        try {
            jwtUtils.isTokenValid(token, TokenType.REFRESH_TOKEN);
        } catch (ExpiredJwtException ex) {
            // 401 - token hết hạn → yêu cầu đăng nhập lại
            throw new TokenExpiredException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        } catch (JwtException ex) {
            // 401 - token sai chữ ký hoặc bị giả mạo
            throw new InvalidTokenException("Refresh token không hợp lệ.");
        }

        final String username = jwtUtils.extractUsername(token, TokenType.REFRESH_TOKEN);
        log.info("Refreshing token for user: {}", username);

        // 404 - user không tồn tại trong DB
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserDetails userDetails = userDetailService.loadUserByUsername(username);

        String newAccessToken = jwtUtils.generateToken(userDetails);
        // Giữ lại refresh token cũ — token cũ vẫn còn hạn, không cần tạo mới
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .usedId(user.getId())
                .build();
    }

    /**
     * Đăng xuất — xóa token khỏi DB dựa trên access token trong header "x-token".
     * Ném 400 nếu thiếu token.
     */
    @Override
    public String logout(HttpServletRequest request) {
        final String token = request.getHeader("x-token");

        // 400 - thiếu token trong header
        if (StringUtils.isBlank(token)) {
            throw new InvalidRequestException("Token is missing in header 'x-token'");
        }

        final String username = jwtUtils.extractUsername(token, TokenType.ACCESS_TOKEN);
        Token currentToken = tokenService.getByUserName(username);
        tokenService.delete(currentToken);

        return "Logged out successfully";
    }

    /**
     * Quên mật khẩu — sinh reset token và log ra link/curl command để test.
     * Ném 404 nếu email không tồn tại trong hệ thống.
     */
    @Override
    public String forgotPassword(String email) {
        // 404 - email không tồn tại
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + email));

        UserDetails userDetails = userDetailService.loadUserByUsername(user.getName());
        String resetToken = jwtUtils.generateResetToken(userDetails);

        // TODO: Thực tế nên gửi email, hiện tại log ra để test
        String confirm = String.format(
                "curl --location --request POST 'http://localhost:8080/auth/reset-password' \\\n--data '%s'",
                resetToken
        );
        log.info("Reset password command for user {}: {}", user.getName(), confirm);

        return "Reset password email sent successfully";
    }

    /**
     * Xác thực reset token — kiểm tra token hợp lệ trước khi cho phép đổi mật khẩu.
     * Ném 404 nếu user không tồn tại.
     * Ném 401 InvalidTokenException nếu reset token không hợp lệ hoặc hết hạn.
     */
    @Override
    public String resetPassword(String secretKey) {
        final String username = jwtUtils.extractUsername(secretKey, TokenType.RESET_TOKEN);

        // 404 - user không tồn tại
        userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // 401 - reset token không hợp lệ hoặc hết hạn
        if (!jwtUtils.isTokenValid(secretKey, TokenType.RESET_TOKEN)) {
            throw new InvalidTokenException("Reset token is invalid or expired");
        }

        return "Token is valid. Proceed to change password.";
    }

    /**
     * Đổi mật khẩu — kiểm tra token hợp lệ, password khớp, rồi lưu mật khẩu mới.
     * Ném 404 nếu user không tồn tại.
     * Ném 401 nếu reset token không hợp lệ.
     * Ném 400 nếu password và confirmPassword không khớp.
     */
    @Override
    public String changePassword(PasswordResetDTO passwordResetDTO) {
        final String username = jwtUtils.extractUsername(passwordResetDTO.getSecretKey(), TokenType.RESET_TOKEN);

        // 404 - user không tồn tại
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // 401 - reset token không hợp lệ
        if (!jwtUtils.isTokenValid(passwordResetDTO.getSecretKey(), TokenType.RESET_TOKEN)) {
            throw new InvalidTokenException("Reset token is invalid or expired");
        }

        // 400 - password và confirmPassword không khớp
        if (!passwordResetDTO.getPassword().equals(passwordResetDTO.getConfirmPassword())) {
            throw new InvalidRequestException("Password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(passwordResetDTO.getConfirmPassword()));
        userService.savePassword(user);

        return "Password changed successfully";
    }
}
