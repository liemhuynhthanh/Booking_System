package com.huynhliem.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime startTime;
    private String status;
}
