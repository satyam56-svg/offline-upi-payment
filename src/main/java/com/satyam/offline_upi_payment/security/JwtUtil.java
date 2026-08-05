package com.satyam.offline_upi_payment.security;

import com.satyam.offline_upi_payment.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
    }

    public String generateToken(String upiId) {

        return Jwts.builder()
                .subject(upiId)
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(
                        System.currentTimeMillis() + jwtConfig.getExpiration()
                ))
                .signWith(getSigningKey())
                .compact();
    }

}