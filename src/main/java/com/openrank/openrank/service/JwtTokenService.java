package com.openrank.openrank.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtTokenService {

    private final String secret;
    private final long ttlMinutes;

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.ttl-minutes:30}") long ttlMinutes) {
        this.secret = secret;
        this.ttlMinutes = ttlMinutes;
    }

    public String issueToken(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttlMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey())
                .compact();
    }

    public Long parseUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            return subject == null ? null : Long.parseLong(subject);
        } catch (Exception ex) {
            return null;
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
