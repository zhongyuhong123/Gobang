package com.yangshengzhou.gobang.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:come-xiaqi-jwt-secret-key-2024-default}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenExpiration;

    @Value("${jwt.issuer:gobang-platform}")
    private String issuer;

    @Value("${jwt.audience:gobang-users}")
    private String audience;

    public String generateAccessToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "access");
        return createToken(claims, userId, accessTokenExpiration);
    }

    public String generateRefreshToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");
        return createToken(claims, userId, refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token) && isAccessToken(token) && !isTokenExpired(token);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token) && isRefreshToken(token) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    public String getUsernameFromToken(String token) {
        return (String) getAllClaimsFromToken(token).get("username");
    }

    public String getUserIdFromToken(String token) {
        return (String) getAllClaimsFromToken(token).get("userId");
    }

    public String getTokenType(String token) {
        return (String) getAllClaimsFromToken(token).get("tokenType");
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    public long getRemainingTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remainingTime / 1000);
    }
}