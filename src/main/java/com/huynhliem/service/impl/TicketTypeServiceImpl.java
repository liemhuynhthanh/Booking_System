package com.huynhliem.service.impl;

import com.huynhliem.dto.response.TicketTypeResponse;
import com.huynhliem.model.TicketType;
import com.huynhliem.repository.TicketTypeRepository;
import com.huynhliem.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    @Override
    public List<TicketTypeResponse> getTicketTypesByConcertId(Long concertId) {
        List<TicketType> ticketTypes = ticketTypeRepository.findByConcertId(concertId);
        return ticketTypes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private TicketTypeResponse mapToResponse(TicketType ticketType) {
        return TicketTypeResponse.builder()
                .id(ticketType.getId())
                .name(ticketType.getName())
                .price(ticketType.getPrice())
                .totalQuantity(ticketType.getTotalQuantity())
                .remainingQuantity(ticketType.getRemainingQuantity())
                .concertId(ticketType.getConcert().getId())
                .build();
    }
}
