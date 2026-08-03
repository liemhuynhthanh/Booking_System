package com.huynhliem.controller;

import com.huynhliem.dto.request.PasswordResetDTO;
import com.huynhliem.dto.request.UserCreationRequest;
import com.huynhliem.dto.request.UserLoginRequest;
import com.huynhliem.dto.response.BaseResponse;
import com.huynhliem.dto.response.TokenResponse;
import com.huynhliem.dto.response.UserResponse;
import com.huynhliem.service.AuthenticationService;
import com.huynhliem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

        @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới với email chưa được đăng ký. Password được mã hóa bcrypt trước khi lưu.", security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Đăng ký thành công", content = @Content(schema = @Schema(implementation = UserResponse.class), examples = @ExampleObject(value = """
                                        {
                                          "code": 201,
                                          "message": "User registered successfully",
                                          "data": { "id": 1, "name": "nguyenvana", "email": "nguyenvana@gmail.com" },
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }"""))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (validation failed)", content = @Content(examples = @ExampleObject(value = """
                                        {
                                          "code": 400,
                                          "message": "email: must be a well-formed email address",
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }"""))),
                        @ApiResponse(responseCode = "409", description = "Email đã tồn tại", content = @Content(examples = @ExampleObject(value = """
                                        {
                                          "code": 409,
                                          "message": "Email already exists: nguyenvana@gmail.com",
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }""")))
        })
        @PostMapping("/signup")
        public ResponseEntity<BaseResponse<UserResponse>> signup(@Valid @RequestBody UserCreationRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                BaseResponse.<UserResponse>builder()
                                                .code(HttpStatus.CREATED.value())
                                                .message("User registered successfully")
                                                .data(userService.save(request))
                                                .build());
        }

        @Operation(summary = "Đăng nhập", description = "Xác thực username/password, trả về accessToken và refreshToken. AccessToken dùng trong header `Authorization: Bearer <token>`.", security = @SecurityRequirement(name = "") // endpoint
                                                                                                                                                                                                                                     // này
                                                                                                                                                                                                                                     // không
                                                                                                                                                                                                                                     // cần
                                                                                                                                                                                                                                     // auth
        )
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công", content = @Content(schema = @Schema(implementation = TokenResponse.class), examples = @ExampleObject(value = """
                                        {
                                          "code": 200,
                                          "message": "Login successful",
                                          "data": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                                            "usedId": 1
                                          },
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }"""))),
                        @ApiResponse(responseCode = "401", description = "Sai username hoặc password", content = @Content(examples = @ExampleObject(value = """
                                        {
                                          "code": 401,
                                          "message": "Invalid username or password",
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }""")))
        })
        @PostMapping("/login")
        public ResponseEntity<BaseResponse<TokenResponse>> login(@RequestBody UserLoginRequest request) {
                return ResponseEntity.ok(
                                BaseResponse.<TokenResponse>builder()
                                                .code(200)
                                                .message("Login successful")
                                                .data(authenticationService.authenticate(request))
                                                .build());
        }

        @Operation(summary = "Đăng xuất", description = "Xóa token khỏi DB. Gửi accessToken qua header `x-token`.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
                        @ApiResponse(responseCode = "400", description = "Thiếu token trong header x-token")
        })
        @PostMapping("/logout")
        public ResponseEntity<BaseResponse<String>> logout(HttpServletRequest request) {
                return ResponseEntity.ok(
                                BaseResponse.<String>builder()
                                                .code(200)
                                                .message("Logged out successfully")
                                                .data(authenticationService.logout(request))
                                                .build());
        }

        @Operation(summary = "Làm mới Access Token", description = "Dùng refresh token (gửi qua header `x-token`) để lấy access token mới mà không cần đăng nhập lại.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Refresh thành công"),
                        @ApiResponse(responseCode = "401", description = "Refresh token hết hạn hoặc không hợp lệ")
        })
        @PostMapping("/refresh")
        public ResponseEntity<BaseResponse<TokenResponse>> refreshToken(HttpServletRequest request) {
                return ResponseEntity.ok(
                                BaseResponse.<TokenResponse>builder()
                                                .code(200)
                                                .message("Token refreshed successfully")
                                                .data(authenticationService.refresh(request))
                                                .build());
        }

        @Operation(summary = "Quên mật khẩu", description = "Nhập email để nhận link reset mật khẩu. Thực tế sẽ gửi email, hiện tại log ra console để test.", security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Gửi email thành công"),
                        @ApiResponse(responseCode = "404", description = "Email không tồn tại trong hệ thống")
        })
        @PostMapping("/forgot-password")
        public ResponseEntity<BaseResponse<String>> forgotPassword(@RequestBody String email) {
                return ResponseEntity.ok(
                                BaseResponse.<String>builder()
                                                .code(200)
                                                .message("Reset password email sent")
                                                .data(authenticationService.forgotPassword(email))
                                                .build());
        }

        @Operation(summary = "Xác thực reset token", description = "Kiểm tra reset token hợp lệ trước khi cho phép đổi mật khẩu. Gửi token trong body.", security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Token hợp lệ"),
                        @ApiResponse(responseCode = "401", description = "Token không hợp lệ hoặc đã hết hạn")
        })
        @PostMapping("/reset-password")
        public ResponseEntity<BaseResponse<String>> resetPassword(@RequestBody String secretKey) {
                return ResponseEntity.ok(
                                BaseResponse.<String>builder()
                                                .code(200)
                                                .message("Token is valid")
                                                .data(authenticationService.resetPassword(secretKey))
                                                .build());
        }

        @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu mới sau khi đã xác thực reset token. Cần gửi secretKey, password mới và confirmPassword.", security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
                        @ApiResponse(responseCode = "400", description = "Password và confirmPassword không khớp"),
                        @ApiResponse(responseCode = "401", description = "Reset token không hợp lệ")
        })
        @PostMapping("/change-password")
        public ResponseEntity<BaseResponse<String>> changePassword(@RequestBody PasswordResetDTO passwordResetDTO) {
                return ResponseEntity.ok(
                                BaseResponse.<String>builder()
                                                .code(200)
                                                .message("Password changed successfully")
                                                .data(authenticationService.changePassword(passwordResetDTO))
                                                .build());
        }
}
