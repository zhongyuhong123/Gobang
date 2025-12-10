package com.yangshengzhou.gobang.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 工具类
 * 实现双token机制：access token + refresh token
 */
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:come-xiaqi-jwt-secret-key-2024-default}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long accessTokenExpiration; // access token 有效期（默认24小时）

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenExpiration; // refresh token 有效期（默认7天）

    @Value("${jwt.issuer:gobang-platform}")
    private String issuer;

    @Value("${jwt.audience:gobang-users}")
    private String audience;

    /**
     * 生成 access token
     */
    public String generateAccessToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "access");
        return createToken(claims, userId, accessTokenExpiration);
    }

    /**
     * 生成 refresh token
     */
    public String generateRefreshToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");
        return createToken(claims, userId, refreshTokenExpiration);
    }

    /**
     * 创建token
     */
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

    /**
     * 从token中获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证token是否有效
     */
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

    /**
     * 验证access token
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token) && isAccessToken(token) && !isTokenExpired(token);
    }

    /**
     * 验证refresh token
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token) && isRefreshToken(token) && !isTokenExpired(token);
    }

    /**
     * 验证token是否过期
     */
    public boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return (String) getAllClaimsFromToken(token).get("username");
    }

    /**
     * 从token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        return (String) getAllClaimsFromToken(token).get("userId");
    }

    /**
     * 从token中获取token类型
     */
    public String getTokenType(String token) {
        return (String) getAllClaimsFromToken(token).get("tokenType");
    }

    /**
     * 验证token是否为access token
     */
    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    /**
     * 验证token是否为refresh token
     */
    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    /**
     * 获取token剩余有效时间（秒）
     */
    public long getRemainingTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remainingTime / 1000);
    }
}