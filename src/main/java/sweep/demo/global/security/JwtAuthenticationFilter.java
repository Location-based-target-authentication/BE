package sweep.demo.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

// JWT Access Token 검사하는 역할
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        System.out.println("[JwtAuthenticationFilter] 요청 URI: " + requestUri);

        if (requestUri.contains("/api/v1/auth/refresh")) {
            System.out.println("[JwtAuthenticationFilter] Refresh Token 검증 요청 - JWT 인증 건너뜀");
            chain.doFilter(request, response);
            return;
        }

        // Google / Kakao Callback 요청은 JWT 인증을 건너뜀
        if (requestUri.contains("/api/v1/auth/google/callback") || requestUri.contains("/api/v1/auth/kakao/callback")) {
            System.out.println("[JwtAuthenticationFilter] OAuth Callback 요청 - JWT 인증 건너뜀");
            chain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 JWT Access Token 가져오기
        String token = resolveToken(request);
        System.out.println("[JwtAuthenticationFilter] 요청 Authorization 헤더: " + request.getHeader("Authorization"));
        System.out.println("[JwtAuthenticationFilter] 추출된 토큰: " + token);

        if (token != null) {
            try {
                if (jwtUtil.validateToken(token, false)) {
                    Authentication authentication = jwtUtil.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[DEBUG] JWT 인증 성공 - 사용자: " + authentication.getName());
                } else {
                    throw new JwtException("유효하지 않은 토큰");
                }
            } catch (ExpiredJwtException e) {
                System.out.println("[JwtAuthenticationFilter] Access Token 만료!");
                sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED", "Access Token이 만료되었습니다. Refresh Token을 사용하세요.");
                return;
            } catch (JwtException e) {
                System.out.println("[JwtAuthenticationFilter] 유효하지 않은 토큰!");
                sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 Access Token입니다.");
                return;
            }
        }
        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println("[JwtAuthenticationFilter] 받은 Authorization 헤더: " + bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 실제 토큰 값 반환
        }
        System.out.println("[JwtAuthenticationFilter] Authorization 헤더에 JWT 토큰이 없음!");
        return null;
    }
    private void sendJsonErrorResponse(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(
                "error", error,
                "message", message
        )));
    }
}


