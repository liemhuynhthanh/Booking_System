package com.huynhliem.service.impl;

import com.huynhliem.dto.response.ConcertResponse;
import com.huynhliem.model.Concert;
import com.huynhliem.repository.ConcertRepository;
import com.huynhliem.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ConcertServiceImpl implements ConcertService {

    private final ConcertRepository concertRepository;

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
}
