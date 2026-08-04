package com.huynhliem.service;

import com.huynhliem.dto.request.BookingRequest;
import com.huynhliem.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BookingResponse reserveTickets(BookingRequest request);
    BookingResponse getBookingById(Long bookingId);
    List<BookingResponse> getMyBookings();
    Page<BookingResponse> getAllBookingsForAdmin(Pageable pageable);
    BookingResponse updateBookingStatus(Long bookingId, String newStatus);
}
