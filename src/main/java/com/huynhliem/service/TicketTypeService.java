package com.huynhliem.service;

import com.huynhliem.dto.response.TicketTypeResponse;

import java.util.List;

public interface TicketTypeService {
    List<TicketTypeResponse> getTicketTypesByConcertId(Long concertId);
}
