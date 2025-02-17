package sweep.demo.global.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sweep.demo.exception.RefreshTokenExpiredException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private final Key secretKey;
    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    public JwtUtil(@Value("${jwt.secret-key}") String secret) {
        System.out.println("[JWTUtil] 설정된 비밀키: "+ secret);
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    // Access Token 생성
    public String generateAccessToken(String userId) {
        String token = Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256) // 환경 변수에서 가져온 키 사용
                .compact();
        System.out.println("[JWTUtil] 생성된 access token: "+ token);
        return token;
    }
    // Refresh Token 생성
    public String generateRefreshToken(String socialId) {
        return Jwts.builder()
                .setSubject(socialId)
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256) // 환경 변수에서 가져온 키 사용
                .compact();
    }
// JWT 토큰 검증
public boolean validateToken(String token, boolean isRefreshToken) {
    System.out.println("[DEBUG] validateToken() 호출됨 - isRefreshToken: " + isRefreshToken);
    System.out.println("[DEBUG] 검증할 토큰: " + token);
    if (token == null || token.isEmpty()) {
        return false;
    }
    try {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
        return true;
    } catch (ExpiredJwtException e) {
        if (isRefreshToken) {
            System.out.println("[JwtUtil] Refresh Token이 만료됨!");
            throw new RefreshTokenExpiredException("Refresh Token이 만료되었습니다.");
        } else { // Access Token이 만료된 경우
            System.out.println("[JwtUtil] Access Token이 만료됨!");
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "TOKEN_EXPIRED");
        }
    } catch (JwtException e) {
        System.out.println("[JwtUtil] 유효하지 않은 토큰: " + e.getMessage());
        return false;
    }
}
    // JWT 토큰에서 사용자 ID 추출
    public String extractUserId(String token) {
        String userId= Jwts.parserBuilder()
                .setSigningKey(secretKey)
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

