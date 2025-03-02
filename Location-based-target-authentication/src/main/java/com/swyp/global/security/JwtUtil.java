package com.swyp.global.security;

import com.swyp.exception.RefreshTokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-expiration}") String accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") String refreshTokenExpiration) {

        try {
            System.out.println("Secret Key (Encoded): " + secretKey);
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            System.out.println("Secret Key Length (bits): " + keyBytes.length * 8);

            if (keyBytes.length * 8 < 256) {
                throw new WeakKeyException("The JWT secret key minimum 256 bits required.");
            }

            this.key = Keys.hmacShaKeyFor(keyBytes);
            this.accessTokenExpiration = Long.parseLong(accessTokenExpiration);
            this.refreshTokenExpiration = Long.parseLong(refreshTokenExpiration);

        } catch (IllegalArgumentException e) {
            throw new WeakKeyException("Invalid JWT secret key format: " + e.getMessage());
        }
    }

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, boolean isRefreshToken) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            if (isRefreshToken) {
                throw new RefreshTokenExpiredException("Refresh Token이 만료되었습니다.");
            } else {
                throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "TOKEN_EXPIRED");
            }
        } catch (JwtException e) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userIdStr = claims.getSubject();
            if (userIdStr == null || userIdStr.isEmpty()) {
                System.out.println("[JWTUtil] JWT에서 userId 추출 오류");
                return null;
            } else {
                Long userId = Long.parseLong(userIdStr);
                System.out.println("[JwtUtil] 추출된 userId: " + userId);
                return userId;
            }
        } catch (JwtException e) {
            System.out.println("[JWTUtil] JWT 파싱 오류: " + e.getMessage());
            return null;
        }
    }

    public Authentication getAuthentication(String token) {
        Long userId = extractUserId(token);
        if (userId == null) {
            throw new JwtException("Invalid JWT token");
        }
        UserDetails userDetails = new User(String.valueOf(userId), "", Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }
}