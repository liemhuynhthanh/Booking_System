package com.huynhliem.repository;

import com.huynhliem.dto.response.UserResponse;
import com.huynhliem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByName(String username);

    Optional<User> findUserByEmail(String email);
}
