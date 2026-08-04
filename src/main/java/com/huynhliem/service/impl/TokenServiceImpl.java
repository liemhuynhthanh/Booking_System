package com.huynhliem.service.impl;

import com.huynhliem.model.Token;
import com.huynhliem.repository.TokenRepository;
import com.huynhliem.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.naming.NameNotFoundException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;

    @Override
    public Long save(Token token) {
        tokenRepository.save(token);
        return token.getId();
    }

    @Override
    public String delete(Token token) {
        tokenRepository.delete(token);
        return "Token deleted successfully";
    }

    @Override
    public Token getByToken(String token) {
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

    @Override
    public void revokeAllTokensForUser(Long userId) {
        java.util.List<Token> validTokens = tokenRepository.findAllByUserIdAndRevokedFalse(userId);
        if (validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> token.setRevoked(true));
        tokenRepository.saveAll(validTokens);
    }
}
