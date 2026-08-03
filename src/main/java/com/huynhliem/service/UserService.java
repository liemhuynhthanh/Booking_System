package com.huynhliem.service;

import com.huynhliem.dto.request.UserCreationRequest;
import com.huynhliem.dto.response.UserResponse;
import com.huynhliem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse save(UserCreationRequest req);
    User savePassword(User user);
    void update(UserCreationRequest req);
    void delete(Long id);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse getUserByName(String name);
    Page<UserResponse> findAll(String keyword, Pageable pageable);
}
