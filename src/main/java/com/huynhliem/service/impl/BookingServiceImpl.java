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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // 3. Kiểm tra Concert
        Concert concert = concertRepository.findById(request.getConcertId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Concert not found with id: " + request.getConcertId()));

        if (!"UPCOMING".equals(concert.getStatus()) && !"ONGOING".equals(concert.getStatus())) {
            throw new InvalidRequestException("Cannot book tickets for a concert that is " + concert.getStatus());
        }

        // --- BẮT ĐẦU: CHỐNG ÔM VÉ (ANTI-SCALPING) ---
        // 1. Kiểm tra số lượng vé tối đa mỗi đơn (Max 4 vé/đơn)
        int totalQuantityRequested = request.getItems().stream()
                .mapToInt(BookingItemRequest::getQuantity)
                .sum();
        if (totalQuantityRequested > 4) {
            throw new InvalidRequestException("Bạn chỉ được phép mua tối đa 4 vé cho mỗi đơn hàng.");
        }

        // 2. Kiểm tra số đơn hàng PENDING tối đa của User (Max 2 đơn PENDING)
        long pendingOrdersCount = bookingRepository.countByUserIdAndStatus(user.getId(), "PENDING");
        if (pendingOrdersCount >= 2) {
            throw new InvalidRequestException("Bạn đang có quá nhiều đơn hàng chưa thanh toán. Vui lòng thanh toán hoặc hủy đơn cũ trước khi đặt tiếp.");
        }
        // --- KẾT THÚC: CHỐNG ÔM VÉ ---

        // 4. Xử lý từng BookingItem
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingItem> bookingItemsToSave = new ArrayList<>();

        for (BookingItemRequest itemReq : request.getItems()) {
            TicketType ticketType = ticketTypeRepository.findById(itemReq.getTicketTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ticket type not found with id: " + itemReq.getTicketTypeId()));

            if (!ticketType.getConcert().getId().equals(concert.getId())) {
                throw new InvalidRequestException(
                        "Ticket type " + ticketType.getName() + " does not belong to concert " + concert.getTitle());
            }

            if (ticketType.getRemainingQuantity() < itemReq.getQuantity()) {
                throw new InvalidRequestException("Not enough tickets available for type: " + ticketType.getName());
            }

            // Trừ số lượng an toàn bằng câu lệnh DB trực tiếp
            int updatedRows = ticketTypeRepository.decrementTicketQuantity(ticketType.getId(), itemReq.getQuantity());
            if (updatedRows == 0) {
                throw new InvalidRequestException(
                        "Tickets sold out or not enough tickets available for type: " + ticketType.getName());
            }

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
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Voucher not found with code: " + request.getVoucherCode()));

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
                .discountAmount(discountAmount)
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

        return mapToResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found or access denied"));

        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<BookingResponse> responses = new ArrayList<>();
        for (Booking booking : bookings) {
            responses.add(mapToResponse(booking));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookingsForAdmin(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!"ADMIN".equals(user.getRole().getRoleName())) {
            throw new InvalidRequestException("Access denied. Admin role required.");
        }

        Page<Booking> bookings = bookingRepository.findAll(pageable);
        return bookings.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, String newStatus) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!"ADMIN".equals(user.getRole().getRoleName())) {
            throw new InvalidRequestException("Access denied. Admin role required.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        String currentStatus = booking.getStatus();
        if (currentStatus.equals(newStatus)) {
            return mapToResponse(booking);
        }
        // Trạng thái đã Hủy hoặc Hết hạn thì KHÔNG thể khôi phục
        if ("CANCELLED".equals(currentStatus) || "EXPIRED".equals(currentStatus)) {
            throw new InvalidRequestException(
                    "Cannot change status of a cancelled or expired booking. Please create a new one.");
        }

        // Nếu chuyển từ trạng thái đang giữ vé (PENDING/PAID) sang Hủy
        // (CANCELLED/EXPIRED)
        boolean wasActive = "PENDING".equals(currentStatus) || "PAID".equals(currentStatus);
        boolean isNowCancelled = "CANCELLED".equals(newStatus) || "EXPIRED".equals(newStatus);

        if (wasActive && isNowCancelled) {
            // Hoàn lại vé
            for (BookingItem item : booking.getBookingItems()) {
                ticketTypeRepository.incrementTicketQuantity(item.getTicketType().getId(), item.getQuantity());
            }

            // Hoàn lại lượt dùng voucher
            if (booking.getVoucher() != null) {
                Voucher voucher = booking.getVoucher();
                voucher.setUsedCount(voucher.getUsedCount() - 1);
                voucherRepository.save(voucher);
            }
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);

        log.info("Admin updated booking ID: {} status from {} to {}", bookingId, currentStatus, newStatus);
        return mapToResponse(booking);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BigDecimal discountAmount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
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

    @Transactional
    @Scheduled(fixedRate = 60000) // Run every 1 minute
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore("PENDING", now);

        if (!expiredBookings.isEmpty()) {
            log.info("Found {} expired pending bookings to cancel.", expiredBookings.size());
        }
        for (Booking booking : expiredBookings) {
            // 1. Change status
            booking.setStatus("EXPIRED");

            // 2. Return ticket quantities
            for (BookingItem item : booking.getBookingItems()) {
                ticketTypeRepository.incrementTicketQuantity(item.getTicketType().getId(), item.getQuantity());
            }

            // 3. Return voucher usage if a voucher was applied
            if (booking.getVoucher() != null) {
                Voucher voucher = booking.getVoucher();
                voucher.setUsedCount(voucher.getUsedCount() - 1);
                voucherRepository.save(voucher);
            }

            bookingRepository.save(booking);
            log.info("Cancelled expired booking ID: {}", booking.getId());
        }
    }
}
