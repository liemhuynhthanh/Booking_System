package com.huynhliem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertAvailabilityResponse {
    private Long concertId;
    private String title;
    private String status;
    private Integer totalTickets;
    private Integer remainingTickets;
    private Integer soldTickets;
    private List<TicketTypeResponse> ticketTypeDetails;
}
