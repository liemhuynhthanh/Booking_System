package com.huynhliem.repository;

import com.huynhliem.model.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long> {
    Page<Concert> findByStatus(String status, Pageable pageable);
    Page<Concert> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Concert> findByTitleContainingIgnoreCaseAndStatus(String title, String status, Pageable pageable);
}
