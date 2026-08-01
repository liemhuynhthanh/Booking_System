package com.huynhliem.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${jwt.expiredHours}")
    private long expiredHours;
    @Value("${jwt.expiredDays}")
    private long expiredDays;

    @Value("${jwt.sercretKey}")
    private String secretKey;
    @Value("${jwt.refreshSecretKey}")
    private String refreshSecretKey;
    @Value("${jwt.resetSecretKey}")
    private String resetKey;

    /**
     * Sinh access token từ UserDetails.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * expiredHours))
                .signWith(getKey(TokenType.ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * expiredDays)) // Refresh token expires in 7 days
                .signWith(getKey(TokenType.REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateResetToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60)) // Reset token expires in 1 hour
                .signWith(getKey(TokenType.RESET_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Lấy signing key từ Base64-encoded secret.
     */
    public Key getKey(TokenType tokenType) {
        byte[] keyBytes;
        if (tokenType.equals(TokenType.ACCESS_TOKEN)) {
            keyBytes = Decoders.BASE64.decode(secretKey);
        }
        else if (tokenType.equals(TokenType.RESET_TOKEN)) {
            keyBytes = Decoders.BASE64.decode(resetKey);
        }
        else {
            // Handle refresh token key if different
            keyBytes = Decoders.BASE64.decode(refreshSecretKey);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
    private <T> T extractClaim(String token, TokenType type, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaim(token, type);
        return claimsResolver.apply(claims);
    }



    public Claims extractAllClaim(String token, TokenType type){
        return Jwts.parserBuilder()
                .setSigningKey(getKey(type))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String extractUsername(String token, TokenType type){
        return extractClaim(token,type,Claims::getSubject);
    }

    // Check if the token is valid (The token is not expired, the signature is valid, the token is not empty)
    public boolean isTokenValid(String token,TokenType type) {
        try {
            Claims c = extractAllClaim(token, type);
            return c.getExpiration().after(Date.from(Instant.now()));
        } catch (ExpiredJwtException ex) {
            throw new JwtException("Token is expired!");
        } catch (MalformedJwtException ex) {
            throw new JwtException("Token is invalid with structure!");
        } catch (SignatureException ex) {
            throw new JwtException("Token is invalid with signature!");
        } catch (IllegalArgumentException ex) {
            throw new JwtException("Token is empty or not existed!");
        } catch (JwtException ex) {
            throw new JwtException("Token is invalid!");
        }
    }
}
