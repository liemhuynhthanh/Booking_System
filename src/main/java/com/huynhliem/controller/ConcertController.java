package com.huynhliem.controller;

import com.huynhliem.dto.response.BaseResponse;
import com.huynhliem.dto.response.ConcertAvailabilityResponse;
import com.huynhliem.dto.response.ConcertResponse;
import com.huynhliem.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.huynhliem.dto.request.ConcertRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/concerts")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Concert", description = "API cho buổi ca nhạc")
public class ConcertController {

        private final ConcertService concertService;

        @Operation(summary = "Tạo mới buổi ca nhạc (Admin)", description = "Tạo buổi ca nhạc mới và phát hành vé cùng lúc. Yêu cầu quyền Admin.")
        @PostMapping("/admin/create")
        public ResponseEntity<BaseResponse<ConcertResponse>> createConcert(@Valid @RequestBody ConcertRequest request) {
                ConcertResponse response = concertService.createConcert(request);
                return ResponseEntity.ok(
                                BaseResponse.<ConcertResponse>builder()
                                                .code(200)
                                                .message("Concert created and tickets issued successfully")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Thống kê vé còn lại (Admin)", description = "Lấy tổng quan số lượng vé tổng, đã bán, còn trống của một buổi diễn.")
        @GetMapping("/admin/{id}/availability")
        public ResponseEntity<BaseResponse<ConcertAvailabilityResponse>> getConcertAvailability(@PathVariable Long id) {
                ConcertAvailabilityResponse response = concertService.getConcertAvailability(id);
                return ResponseEntity.ok(
                                BaseResponse.<ConcertAvailabilityResponse>builder()
                                                .code(200)
                                                .message("Concert availability retrieved successfully")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Danh sách buổi ca nhạc", description = """
                        Lấy danh sách buổi ca nhạc có phân trang, tìm kiếm theo tên và trạng thái.

                        **Tham số:**
                        - `keyword`: tìm kiếm theo tên (title)
                        - `status`: lọc theo trạng thái (ví dụ: UPCOMING, ONGOING, ENDED, CANCELLED)
                        - `page`: trang hiện tại (bắt đầu từ 0, mặc định: 0)
                        - `size`: số bản ghi mỗi trang (mặc định: 20)
                        - `sort`: sắp xếp theo trường (mặc định: `startTime,asc`)
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(examples = @ExampleObject(value = """
                                        {
                                          "code": 200,
                                          "message": "Concerts retrieved successfully",
                                          "data": [
                                            { "id": 1, "title": "Show Âm Nhạc Mùa Thu", "startTime": "2026-10-15T19:00:00", "status": "UPCOMING" }
                                          ],
                                          "currentPage": 0,
                                          "pageSize": 20,
                                          "totalItems": 10,
                                          "totalPages": 1,
                                          "timestamp": "2026-08-03T15:00:00Z"
                                        }""")))
        })
        @GetMapping("/list")
        public ResponseEntity<BaseResponse<List<ConcertResponse>>> getAllConcerts(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String status,
                        @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable) {

                Page<ConcertResponse> page = concertService.findAll(keyword, status, pageable);

                return ResponseEntity.ok(
                                BaseResponse.<List<ConcertResponse>>builder()
                                                .code(200)
                                                .message("Concerts retrieved successfully")
                                                .data(page.getContent())
                                                .currentPage(page.getNumber())
                                                .pageSize(page.getSize())
                                                .totalItems(page.getTotalElements())
                                                .totalPages(page.getTotalPages())
                                                .build());
        }
}
