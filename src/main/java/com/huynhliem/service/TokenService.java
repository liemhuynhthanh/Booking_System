package com.huynhliem.service;

import com.huynhliem.model.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

public interface TokenService {
    Long save(Token token);
    String delete(Token token);
    Token getByUserName(String username);

}
