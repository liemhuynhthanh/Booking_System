package com.huynhliem.service.impl;

import com.huynhliem.dto.request.UserChangePasswordRequest;
import com.huynhliem.dto.request.UserCreationRequest;
import com.huynhliem.dto.response.UserResponse;
import com.huynhliem.model.Role;
import com.huynhliem.model.User;
import com.huynhliem.repository.RoleRepository;
import com.huynhliem.repository.UserRepository;
import com.huynhliem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse save(UserCreationRequest req) {
        if (userRepository.findUserByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + req.getEmail());
        }
        // Gán role USER mặc định
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    return roleRepository.save(Role.builder().roleName("USER").build());
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(userRole)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully with id: {}", saved.getId());

        return toUserResponse(saved);
    }

    @Override
    public void update(UserCreationRequest req) {
        // TODO: implement update
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void changePassword(UserChangePasswordRequest req) {
        // TODO: implement changePassword
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return toUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        return toUserResponse(user);
    }

    @Override
    public UserResponse getUserByName(String name) {
        User user = userRepository.findUserByName(name)
                .orElseThrow(() -> new IllegalArgumentException("User not found with name: " + name));
        return toUserResponse(user);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
