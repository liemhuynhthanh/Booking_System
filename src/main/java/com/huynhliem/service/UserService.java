package com.huynhliem.service;

import com.huynhliem.dto.request.UserChangePasswordRequest;
import com.huynhliem.dto.request.UserCreationRequest;
import com.huynhliem.dto.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    UserResponse save(UserCreationRequest req);
    void update(UserCreationRequest req);
    void delete(Long id);
    void changePassword(UserChangePasswordRequest req);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse getUserByName(String name);
    List<UserResponse> findAll();
}
