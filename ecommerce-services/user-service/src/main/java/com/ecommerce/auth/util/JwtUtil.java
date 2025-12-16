package com.ecommerce.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;  // milliseconds (e.g., 86400000 = 24 hours)

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;  // milliseconds (e.g., 604800000 = 7 days)

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ========================
    // FOR FRONTEND (seconds)
    // ========================
    public Long getAccessTokenExpirationInSeconds() {
        return expiration / 1000;
    }

    public Long getRefreshTokenExpirationInSeconds() {
        return refreshExpiration / 1000;
    }

    // ========================
    // FOR INTERNAL USE (milliseconds)
    // ========================
    public Long getAccessTokenExpirationInMillis() {
        return expiration;
    }

    public Long getRefreshTokenExpirationInMillis() {
        return refreshExpiration;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}