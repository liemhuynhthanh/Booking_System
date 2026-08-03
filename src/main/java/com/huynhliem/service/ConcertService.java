package com.huynhliem.service;

import com.huynhliem.dto.response.ConcertResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConcertService {
    Page<ConcertResponse> findAll(String keyword, String status, Pageable pageable);
}
