package com.huynhliem.service.impl;

import com.huynhliem.dto.request.BookingItemRequest;
import com.huynhliem.dto.request.BookingRequest;
import com.huynhliem.dto.response.BookingItemResponse;
import com.huynhliem.dto.response.BookingResponse;
import com.huynhliem.exception.InvalidRequestException;
import com.huynhliem.exception.ResourceNotFoundException;
import com.huynhliem.model.*;
import com.huynhliem.repository.*;
import com.huynhliem.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final UserRepository userRepository;
    private final ConcertRepository concertRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final VoucherRepository voucherRepository;

    @Override
    @Transactional
    public BookingResponse reserveTickets(BookingRequest request) {
        // 1. Kiểm tra Idempotency Key (chống duplicate request)
        Optional<Booking> existingBooking = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingBooking.isPresent()) {
            log.info("Idempotency key {} already processed, returning existing booking.", request.getIdempotencyKey());
            return mapToResponse(existingBooking.get());
        }

        // 2. Lấy thông tin User hiện tại
        String username = getUsernameFromContext();
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // 3. Kiểm tra Concert
        Concert concert = concertRepository.findById(request.getConcertId())
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + request.getConcertId()));
        
        if (!"UPCOMING".equals(concert.getStatus()) && !"ONGOING".equals(concert.getStatus())) {
            throw new InvalidRequestException("Cannot book tickets for a concert that is " + concert.getStatus());
        }

        // 4. Xử lý từng BookingItem
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingItem> bookingItemsToSave = new ArrayList<>();
        
        for (BookingItemRequest itemReq : request.getItems()) {
            TicketType ticketType = ticketTypeRepository.findById(itemReq.getTicketTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + itemReq.getTicketTypeId()));
            
            if (!ticketType.getConcert().getId().equals(concert.getId())) {
                throw new InvalidRequestException("Ticket type " + ticketType.getName() + " does not belong to concert " + concert.getTitle());
            }

            if (ticketType.getRemainingQuantity() < itemReq.getQuantity()) {
                throw new InvalidRequestException("Not enough tickets available for type: " + ticketType.getName());
            }

            // Trừ số lượng (Optimistic Locking sẽ được trigger khi transaction commit)
            ticketType.setRemainingQuantity(ticketType.getRemainingQuantity() - itemReq.getQuantity());
            ticketTypeRepository.save(ticketType);

            BigDecimal itemTotal = ticketType.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            BookingItem bookingItem = BookingItem.builder()
                    .quantity(itemReq.getQuantity())
                    .price(ticketType.getPrice()) // Lưu lại giá tại thời điểm đặt
                    .ticketType(ticketType)
                    .build();
            bookingItemsToSave.add(bookingItem);
        }

        // 5. Xử lý Voucher (nếu có)
        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            voucher = voucherRepository.findByCode(request.getVoucherCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + request.getVoucherCode()));
            
            if (voucher.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new InvalidRequestException("Voucher has expired");
            }
            if (voucher.getUsedCount() >= voucher.getMaxUses()) {
                throw new InvalidRequestException("Voucher usage limit reached");
            }

            if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
                discountAmount = totalAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
            } else if ("FIXED_AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
                discountAmount = voucher.getDiscountValue();
            }

            // Giới hạn giảm giá không vượt quá tổng tiền
            if (discountAmount.compareTo(totalAmount) > 0) {
                discountAmount = totalAmount;
            }
            
            totalAmount = totalAmount.subtract(discountAmount);
            
            // Tăng số lượt sử dụng voucher
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
        }

        // 6. Tạo và lưu Booking
        Booking booking = Booking.builder()
                .user(user)
                .concert(concert)
                .voucher(voucher)
                .totalAmount(totalAmount)
                .status("PENDING") // Chờ thanh toán
                .idempotencyKey(request.getIdempotencyKey())
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // Giữ vé 15 phút
                .bookingItems(new ArrayList<>())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // 7. Gắn Booking vào từng BookingItem và lưu
        for (BookingItem item : bookingItemsToSave) {
            item.setBooking(savedBooking);
            savedBooking.getBookingItems().add(item);
        }
        bookingItemRepository.saveAll(bookingItemsToSave);

        return mapToResponse(savedBooking, discountAmount);
    }

    private String getUsernameFromContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (booking.getVoucher() != null) {
            // Tạm thời tính toán lại discountAmount dựa trên loại giảm giá và voucher, hoặc có thể lưu discountAmount vào DB
            // Ở đây vì mapToResponse không có discountAmount, ta chỉ return những gì booking có
            // Ideal là nên thêm discountAmount vào table bookings.
        }
        return mapToResponse(booking, discountAmount);
    }

    private BookingResponse mapToResponse(Booking booking, BigDecimal discountAmount) {
        List<BookingItemResponse> itemResponses = new ArrayList<>();
        if (booking.getBookingItems() != null) {
            for (BookingItem item : booking.getBookingItems()) {
                BigDecimal subTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                itemResponses.add(BookingItemResponse.builder()
                        .id(item.getId())
                        .ticketTypeId(item.getTicketType().getId())
                        .ticketTypeName(item.getTicketType().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subTotal(subTotal)
                        .build());
            }
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .concertId(booking.getConcert().getId())
                .concertTitle(booking.getConcert().getTitle())
                .status(booking.getStatus())
                .idempotencyKey(booking.getIdempotencyKey())
                .totalAmount(booking.getTotalAmount())
                .voucherCode(booking.getVoucher() != null ? booking.getVoucher().getCode() : null)
                .discountAmount(discountAmount)
                .expiresAt(booking.getExpiresAt())
                .items(itemResponses)
                .build();
    }
}
