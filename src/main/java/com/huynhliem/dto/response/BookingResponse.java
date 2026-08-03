package com.huynhliem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long concertId;
    private String concertTitle;
    private String status;
    private String idempotencyKey;
    private BigDecimal totalAmount;
    private String voucherCode;
    private BigDecimal discountAmount;
    private LocalDateTime expiresAt;
    private List<BookingItemResponse> items;
}
