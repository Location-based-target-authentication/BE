package com.swyp.global.security;
import com.swyp.exception.RefreshTokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
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
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = Long.parseLong(accessTokenExpiration);
        this.refreshTokenExpiration = Long.parseLong(refreshTokenExpiration);
    }
    // Access Token 생성
    public String generateAccessToken(String userId) {
        String token = Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256) // 환경 변수에서 가져온 키 사용
                .compact();
        return token;
    }
    // Refresh Token 생성
    public String generateRefreshToken(String socialId) {
        return Jwts.builder()
                .setSubject(socialId)
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256) // 환경 변수에서 가져온 키 사용
                .compact();
    }
// JWT 토큰 검증
public boolean validateToken(String token, boolean isRefreshToken) {
    if (token == null || token.isEmpty()) {
        return false;
    }
    try {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        return true;
    } catch (ExpiredJwtException e) {
        if (isRefreshToken) {
            throw new RefreshTokenExpiredException("Refresh Token이 만료되었습니다.");
        } else { // Access Token이 만료된 경우
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "TOKEN_EXPIRED");
        }
    } catch (JwtException e) {
        return false;
    }
}
    // JWT 토큰에서 사용자 ID 추출
    public String extractUserId(String token) {
        String userId= Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        if(userId==null || userId.isEmpty()){
            System.out.println("[JWTUtil] JWT에서 userId 추출 오류 ");
        }else{
            System.out.println("[JwtUtil] 추출된 userId: "+userId);
        }
        return userId;
    }
    // 토큰에서 사용자 정보 추출 후 spring security에서 사용할 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        String userId = extractUserId(token);
        UserDetails userDetails = new User(userId, "", Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }
}

