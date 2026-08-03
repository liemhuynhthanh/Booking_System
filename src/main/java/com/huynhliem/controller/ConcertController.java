package com.huynhliem.controller;

import com.huynhliem.dto.response.BaseResponse;
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

@RestController
@RequestMapping("/concert")
@Slf4j
@RequiredArgsConstructor
public class ConcertController {

        private final ConcertService concertService;

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
