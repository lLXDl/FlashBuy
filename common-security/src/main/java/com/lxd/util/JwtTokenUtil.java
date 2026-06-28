package com.lxd.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class JwtTokenUtil {
    // 签名秘钥
    @Value("${jwt.secret}")
    private String secret;
    // 过期时间
    @Value("${jwt.expiration}")
    private Long expiration;  // 秒

    // 生成Token
    public String generateToken(Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000)) // 过期时间
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)// 签名
                .compact();
    }

    // 从Token解析Claims
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 校验Token是否有效（不含黑名单校验）
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 从Token中获取用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        // ⚠️ 关键：JSON反序列化后数字默认是Integer，直接强转Long会抛ClassCastException
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    // 从Token中获取角色列表
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        // ⚠️ 关键：泛型在运行时被擦除，必须手动检查元素类型防止脏数据
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item instanceof String)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public long getRemainingExpiration(String token) {
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date now = new Date();
        return (expiration.getTime() - now.getTime()) / 1000; // 返回秒
    }
}
