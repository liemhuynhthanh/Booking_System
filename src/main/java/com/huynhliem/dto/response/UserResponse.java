package com.huynhliem.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Builder
@Setter
public class UserResponse implements Serializable {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
