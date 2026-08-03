package com.huynhliem.service;

import com.huynhliem.dto.request.BookingRequest;
import com.huynhliem.dto.response.BookingResponse;

public interface BookingService {
    BookingResponse reserveTickets(BookingRequest request);
}
