package com.huynhliem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã voucher — unique, người dùng nhập khi đặt vé.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Loại giảm giá: PERCENTAGE (%) hoặc FIXED_AMOUNT (số tiền cụ thể).
     */
    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType;

    /**
     * Giá trị giảm — % nếu PERCENTAGE, số tiền nếu FIXED_AMOUNT.
     */
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /**
     * Giới hạn tổng số lần sử dụng của voucher.
     */
    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;

    /**
     * Số lần đã thực tế sử dụng — dùng để kiểm tra voucher còn lượt dùng không.
     */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    /**
     * Thời hạn hết hạn của voucher.
     */
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    // ── Relations ──────────────────────────────────
    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();
}
