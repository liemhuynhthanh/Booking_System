package com.huynhliem.repository;

import com.huynhliem.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    List<Token> findAllByUserIdAndRevokedFalse(Long userId);
}
