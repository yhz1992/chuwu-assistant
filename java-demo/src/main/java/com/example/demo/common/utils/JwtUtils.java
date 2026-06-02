package com.example.demo.common.utils;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 提供生成和验证 access token / refresh token 的功能
 */
@Slf4j
@Component
public class JwtUtils {

    /** JWT 签名密钥 */
    private final SecretKey secretKey;

    /** access token 过期时间（秒） */
    private final long accessExpire;

    /** refresh token 过期时间（秒） */
    private final long refreshExpire;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.access-expire}") long accessExpire,
                    @Value("${jwt.refresh-expire}") long refreshExpire) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpire = accessExpire;
        this.refreshExpire = refreshExpire;
    }

    /**
     * 生成 access token
     */
    public String generateAccessToken(String userId, String openid) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessExpire * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim("openid", openid)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成 refresh token
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshExpire * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 access token 中获取用户 ID
     */
    public String getUserIdFromAccessToken(String token) {
        return getUserIdFromToken(token, "access");
    }

    /**
     * 从 refresh token 中获取用户 ID
     */
    public String getUserIdFromRefreshToken(String token) {
        return getUserIdFromToken(token, "refresh");
    }

    private String getUserIdFromToken(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!expectedType.equals(claims.get("type", String.class))) {
                log.warn("token类型不匹配，期望{}，实际为: {}", expectedType, claims.get("type"));
                return null;
            }

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("{} token 已过期", expectedType);
            return null;
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("{} token 签名无效或格式错误", expectedType);
            return null;
        } catch (Exception e) {
            log.error("{} token 解析异常", expectedType, e);
            return null;
        }
    }

    /**
     * 验证 token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 token 中获取 openid
     */
    public String getOpenId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("openid", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
