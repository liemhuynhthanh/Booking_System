package com.huynhliem.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings", indexes = {
                @Index(name = "idx_bookings_user_id", columnList = "user_id"),
                @Index(name = "idx_bookings_concert_id", columnList = "concert_id"),
                @Index(name = "idx_bookings_status", columnList = "status"),
                @Index(name = "idx_bookings_expires_at", columnList = "expires_at")
})
public class Booking {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
        private BigDecimal totalAmount;

        @Column(name = "discount_amount", precision = 10, scale = 2)
        private BigDecimal discountAmount;

        @Column(nullable = false, length = 50)
        private String status;

        @Column(name = "idempotency_key", unique = true, length = 100)
        private String idempotencyKey;

        @Column(name = "expires_at", nullable = false)
        private LocalDateTime expiresAt;

        @Column(name = "created_at", updatable = false)
        @CreationTimestamp
        private Instant createdAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "concert_id", nullable = false)
        private Concert concert;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "voucher_id")
        private Voucher voucher;

        @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @Builder.Default
        private List<BookingItem> bookingItems = new ArrayList<>();
}
