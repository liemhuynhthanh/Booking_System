package com.huynhliem.service.impl;

import com.huynhliem.dto.response.ConcertResponse;
import com.huynhliem.model.Concert;
import com.huynhliem.repository.ConcertRepository;
import com.huynhliem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcertServiceImplTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConcertServiceImpl concertService;

    @Test
    void testFindAllConcerts() {
        // Arrange
        Concert concert = new Concert();
        concert.setId(1L);
        concert.setTitle("Test Concert");
        concert.setStatus("PUBLISHED");
        concert.setStartTime(LocalDateTime.now().plusDays(1));

        Page<Concert> concertPage = new PageImpl<>(List.of(concert));
        when(concertRepository.findByTitleContainingIgnoreCaseAndStatus("Test", "PUBLISHED", PageRequest.of(0, 10)))
                .thenReturn(concertPage);

        // Act
        Page<ConcertResponse> response = concertService.findAll("Test", "PUBLISHED", PageRequest.of(0, 10));

        // Assert
        assertEquals(1, response.getTotalElements());
        assertEquals("Test Concert", response.getContent().get(0).getTitle());
        assertEquals("PUBLISHED", response.getContent().get(0).getStatus());
    }
}
