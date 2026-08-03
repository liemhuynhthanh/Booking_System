package com.huynhliem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking_items")
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Số lượng vé đặt của loại vé này trong đơn hàng.
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Giá vé tại thời điểm đặt — lưu lịch sử giá, không bị ảnh hưởng nếu giá thay đổi sau này.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // ── Relations ──────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;
}
