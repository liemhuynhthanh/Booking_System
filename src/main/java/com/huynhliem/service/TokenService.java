package com.huynhliem.service;

import com.huynhliem.model.Token;

public interface TokenService {
    Long save(Token token);

    String delete(Token token);

    Token getByToken(String token);

    void revokeAllTokensForUser(Long userId);

}
