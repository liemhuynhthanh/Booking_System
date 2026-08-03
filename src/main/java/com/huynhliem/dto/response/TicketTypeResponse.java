package com.huynhliem.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private Long concertId;
}
