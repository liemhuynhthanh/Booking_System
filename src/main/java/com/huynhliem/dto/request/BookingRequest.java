package com.huynhliem.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Concert ID is required")
    private Long concertId;
    
    @NotEmpty(message = "At least one ticket type must be selected")
    @Valid
    private List<BookingItemRequest> items;
    
    private String voucherCode;
    
    @NotBlank(message = "Idempotency key is required to prevent duplicate requests")
    private String idempotencyKey;
}
