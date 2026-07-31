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
       java.util.Optional<Token> existingToken = tokenRepository.findTokenByUsername(token.getUsername());
        if (existingToken.isPresent()) {
            Token currentToken= existingToken.get();
            currentToken.setAccessToken(token.getAccessToken());
            currentToken.setRefreshToken(token.getRefreshToken());
            return currentToken.getId();
        }
        else {
            tokenRepository.save(token);
            return token.getId();
        }


    }

    @Override
    public String delete(Token token) {
        tokenRepository.delete(token);
        return "Token deleted successfully";
    }

    @Override
    public Token getByUserName(String username) {
        try {
            return tokenRepository.findTokenByUsername(username).orElseThrow(()->new NameNotFoundException("Token not found"));
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
