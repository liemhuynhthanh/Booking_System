package com.huynhliem.controller;

import com.huynhliem.dto.request.BookingRequest;
import com.huynhliem.dto.response.BaseResponse;
import com.huynhliem.dto.response.BookingResponse;
import com.huynhliem.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "API đặt giữ vé")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Đặt giữ vé", description = "Đặt giữ vé cho một buổi ca nhạc (có hỗ trợ Optimistic Locking). Yêu cầu Bearer Token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đặt giữ vé thành công", content = @Content(examples = @ExampleObject(value = """
                    {
                      "code": 200,
                      "message": "Tickets reserved successfully",
                      "data": {
                        "id": 1,
                        "userId": 1,
                        "concertId": 1,
                        "concertTitle": "Show Âm Nhạc Mùa Thu",
                        "status": "PENDING",
                        "idempotencyKey": "uuid-1234",
                        "totalAmount": 1800000,
                        "voucherCode": "GIAM10",
                        "discountAmount": 200000,
                        "expiresAt": "2026-08-03T15:15:00Z",
                        "items": [
                          {
                            "id": 1,
                            "ticketTypeId": 1,
                            "ticketTypeName": "VIP",
                            "quantity": 1,
                            "price": 2000000,
                            "subTotal": 2000000
                          }
                        ]
                      },
                      "timestamp": "2026-08-03T15:00:00Z"
                    }"""))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc hết vé (OptimisticLockingFailureException/InvalidRequestException)"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy buổi ca nhạc, loại vé hoặc người dùng")
    })
    @PostMapping("/reserve")
    public ResponseEntity<BaseResponse<BookingResponse>> reserveTickets(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.reserveTickets(request);
        return ResponseEntity.ok(
                BaseResponse.<BookingResponse>builder()
                        .code(200)
                        .message("Tickets reserved successfully")
                        .data(response)
                        .build()
        );
    }
}
