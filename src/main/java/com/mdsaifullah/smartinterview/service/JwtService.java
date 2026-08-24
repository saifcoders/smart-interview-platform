package com.mdsaifullah.smartinterview.service;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET environment variable is missing or empty!");
        }
        if (secret.getBytes().length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 256 bits (32 bytes) long for HS256!");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate JWT Token
    public String generateToken(Long userId, String email, String role) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .subject(email)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000 * 60 * 60
                        )
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract User ID
    public Long extractUserId(String token) {

        return extractClaims(token)
                .get("userId", Long.class);
    }

    // Extract Email
    public String extractUsername(String token) {

        return extractClaims(token).getSubject();
    }

    // Extract Role
    public String extractRole(String token) {

        return extractClaims(token)
                .get("role", String.class);
    }

    // Extract Claims
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check Token Expired
    public boolean isTokenExpired(String token) {

        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // Validate Token
    public boolean validateToken(
            String token,
            String email) {

        return extractUsername(token).equals(email)
                && !isTokenExpired(token);
    }
}