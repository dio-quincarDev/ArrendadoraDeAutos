package com.alquiler.car_rent.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alquiler.car_rent.commons.dtos.TokenResponse;
import com.alquiler.car_rent.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {
    
    private final SecretKey secretKey;
    private static final long EXPIRATION_TIME = 864_000_000; // 10 días
    
    public JwtServiceImpl(@Value("${jwt.secret}") String secret) {
        if (secret.getBytes().length < 32) {
            throw new IllegalArgumentException("Secret key must be at least 32 characters long.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public TokenResponse generateToken(Long userEntityId,String role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        String token = Jwts.builder()
                .subject(String.valueOf(userEntityId))
                .claim("userEntityId", userEntityId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
        
        return TokenResponse.builder()
                .accesToken(token)
                .build();
    }
    
    @Override
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            System.err.println("Error Parsing JWT: " + e.getMessage());
            throw new IllegalArgumentException("Invalid JWT Token", e);
        }
    }
    
    @Override
    public boolean isExpired(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    @Override
    public Integer extractUserEntityId(String token) {
        try {
            Claims claims = getClaims(token);
            Object userIdClaim = claims.get("userEntityId"); // Corregido: era "userEntityId"
            if (userIdClaim == null) {
                throw new IllegalArgumentException("No userEntityId claim found for token");
            }
            return ((Number) userIdClaim).intValue(); 
        } catch (Exception e) {
            System.err.println("Error extrayendo token: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }
}