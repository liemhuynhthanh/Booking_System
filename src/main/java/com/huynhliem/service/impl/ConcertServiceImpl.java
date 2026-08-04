package com.huynhliem.service.impl;

import com.huynhliem.dto.response.ConcertResponse;
import com.huynhliem.dto.response.TicketTypeResponse;
import com.huynhliem.model.Concert;
import com.huynhliem.repository.ConcertRepository;
import com.huynhliem.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.huynhliem.dto.request.ConcertRequest;
import com.huynhliem.dto.request.TicketTypeRequest;
import com.huynhliem.dto.response.ConcertAvailabilityResponse;
import com.huynhliem.exception.InvalidRequestException;
import com.huynhliem.exception.ResourceNotFoundException;
import com.huynhliem.model.TicketType;
import com.huynhliem.model.User;
import com.huynhliem.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertServiceImpl implements ConcertService {

    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ConcertResponse createConcert(ConcertRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!"ADMIN".equals(user.getRole().getRoleName())) {
            throw new InvalidRequestException("Access denied. Admin role required.");
        }

        Concert concert = Concert.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .startTime(request.getStartTime())
                .status(request.getStatus() != null ? request.getStatus() : "UPCOMING")
                .ticketTypes(new ArrayList<>())
                .build();

        if (request.getTicketTypes() != null) {
            for (TicketTypeRequest typeReq : request.getTicketTypes()) {
                TicketType ticketType = TicketType.builder()
                        .name(typeReq.getName())
                        .price(typeReq.getPrice())
                        .totalQuantity(typeReq.getTotalQuantity())
                        .remainingQuantity(typeReq.getTotalQuantity())
                        .concert(concert)
                        .build();
                concert.getTicketTypes().add(ticketType);
            }
        }

        Concert savedConcert = concertRepository.save(concert);
        return mapToResponse(savedConcert);
    }

    @Override
    public Page<ConcertResponse> findAll(String keyword, String status, Pageable pageable) {
        Page<Concert> concertPage;

        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasStatus = StringUtils.hasText(status);

        if (hasKeyword && hasStatus) {
            concertPage = concertRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, status, pageable);
        } else if (hasKeyword) {
            concertPage = concertRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else if (hasStatus) {
            concertPage = concertRepository.findByStatus(status, pageable);
        } else {
            concertPage = concertRepository.findAll(pageable);
        }

        return concertPage.map(this::mapToResponse);
    }

    private ConcertResponse mapToResponse(Concert concert) {
        return ConcertResponse.builder()
                .id(concert.getId())
                .title(concert.getTitle())
                .description(concert.getDescription())
                .imageUrl(concert.getImageUrl())
                .startTime(concert.getStartTime())
                .status(concert.getStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConcertAvailabilityResponse getConcertAvailability(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found with id: " + concertId));

        int totalTickets = 0;
        int remainingTickets = 0;
        int soldTickets = 0;

        List<TicketTypeResponse> details = new ArrayList<>();

        for (TicketType type : concert.getTicketTypes()) {
            int typeTotal = type.getTotalQuantity();
            int typeRemaining = type.getRemainingQuantity();
            int typeSold = typeTotal - typeRemaining;

            totalTickets += typeTotal;
            remainingTickets += typeRemaining;
            soldTickets += typeSold;

            details.add(TicketTypeResponse.builder()
                    .id(type.getId())
                    .name(type.getName())
                    .price(type.getPrice())
                    .totalQuantity(typeTotal)
                    .remainingQuantity(typeRemaining)
                    .concertId(concert.getId())
                    .build());
        }

        return ConcertAvailabilityResponse.builder()
                .concertId(concert.getId())
                .title(concert.getTitle())
                .status(concert.getStatus())
                .totalTickets(totalTickets)
                .remainingTickets(remainingTickets)
                .soldTickets(soldTickets)
                .ticketTypeDetails(details)
                .build();
    }
}
