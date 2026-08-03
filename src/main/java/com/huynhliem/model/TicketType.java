package com.huynhliem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_types")
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên loại vé: VIP, Standard, Economy, v.v.
     */
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Tổng số vé phát hành cho loại vé này.
     */
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    /**
     * Số vé còn lại — trừ trực tiếp khi đặt vé để tránh overselling.
     * Sử dụng UPDATE ticket_types SET remaining_quantity = remaining_quantity - ? WHERE id = ? AND remaining_quantity >= ?
     */
    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    /**
     * Version cho Optimistic Locking (phòng ngừa concurrent update).
     * JPA tự động tăng mỗi khi entity được update.
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    // ── Relations ──────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingItem> bookingItems = new ArrayList<>();
}
