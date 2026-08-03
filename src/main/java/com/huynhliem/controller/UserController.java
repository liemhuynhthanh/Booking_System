package com.huynhliem.controller;

import com.huynhliem.dto.request.UserCreationRequest;
import com.huynhliem.dto.response.BaseResponse;
import com.huynhliem.dto.response.UserResponse;
import com.huynhliem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;

        @Operation(summary = "Danh sách user", description = """
                        Lấy danh sách user có phân trang và tìm kiếm theo name/email.

                        **Tham số phân trang:**
                        - `page`: trang hiện tại (bắt đầu từ 0, mặc định: 0)
                        - `size`: số bản ghi mỗi trang (mặc định: 20)
                        - `sort`: sắp xếp theo field (mặc định: `id,asc`)

                        **Ví dụ:** `/user/list?keyword=nguyen&page=0&size=10&sort=name,asc`
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(examples = @ExampleObject(value = """
                                        {
                                          "code": 200,
                                          "message": "Users retrieved successfully",
                                          "data": [
                                            { "id": 1, "name": "nguyenvana", "email": "a@gmail.com", "phone": "0901234567" }
                                          ],
                                          "currentPage": 0,
                                          "pageSize": 20,
                                          "totalItems": 150,
                                          "totalPages": 8,
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }"""))),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực — cần Bearer token")
        })
        @GetMapping("/list")
        public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUser(
                        @RequestParam(required = false) String keyword,
                        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

                Page<UserResponse> page = userService.findAll(keyword, pageable);

                return ResponseEntity.ok(
                                BaseResponse.<List<UserResponse>>builder()
                                                .code(200)
                                                .message("Users retrieved successfully")
                                                .data(page.getContent())
                                                .currentPage(page.getNumber())
                                                .pageSize(page.getSize())
                                                .totalItems(page.getTotalElements())
                                                .totalPages(page.getTotalPages())
                                                .build());
        }

        @Operation(summary = "Chi tiết user", description = "Lấy thông tin chi tiết của một user theo ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công", content = @Content(examples = @ExampleObject(value = """
                                        {
                                          "code": 200,
                                          "message": "User retrieved successfully",
                                          "data": { "id": 1, "name": "nguyenvana", "email": "a@gmail.com" },
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }"""))),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy user với ID này", content = @Content(examples = @ExampleObject(value = """
                                        {
                                          "code": 404,
                                          "message": "User not found with id: 99",
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }"""))),
                        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
        })
        @GetMapping("/{id}")
        public ResponseEntity<BaseResponse<UserResponse>> getUserById(@PathVariable @Min(1) Long id) {
                return ResponseEntity.ok(
                                BaseResponse.<UserResponse>builder()
                                                .code(200)
                                                .message("User retrieved successfully")
                                                .data(userService.getUserById(id))
                                                .build());
        }

        /**
         * Tạo user mới.
         */
        @PostMapping
        public ResponseEntity<BaseResponse<UserResponse>> createUser(@Valid @RequestBody UserCreationRequest req) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                BaseResponse.<UserResponse>builder()
                                                .code(HttpStatus.CREATED.value())
                                                .message("User created successfully")
                                                .data(userService.save(req))
                                                .build());
        }
}
