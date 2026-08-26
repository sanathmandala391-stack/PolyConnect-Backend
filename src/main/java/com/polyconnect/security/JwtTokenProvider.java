package com.polyconnect.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long jwtExpirationInMs;

    public JwtTokenProvider(
        // No default here — must come from JWT_SECRET (see application.yml). If it's missing,
        // Spring fails to start the app rather than silently signing tokens with a known value.
        @Value("${polyconnect.security.jwt.secret}") String secret,
        @Value("${polyconnect.security.jwt.expiration-ms:86400000}") long jwtExpirationInMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userPrincipal.getId());
        claims.put("role", userPrincipal.getRole().name());
        claims.put("status", userPrincipal.getStatus().name());
        claims.put("collegeId", userPrincipal.getCollegeId());
        claims.put("collegeCode", userPrincipal.getCollegeCode());
        claims.put("branchId", userPrincipal.getBranchId());
        claims.put("branchCode", userPrincipal.getBranchCode());

        return Jwts.builder()
            .subject(userPrincipal.getUsername())
            .claims(claims)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getSubject();
    }

    public Claims getClaimsFromJWT(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
