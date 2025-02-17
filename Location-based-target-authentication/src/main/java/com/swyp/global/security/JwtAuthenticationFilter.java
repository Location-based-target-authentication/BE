package com.swyp.global.security;

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
        if (requestUri.contains("/api/v1/auth/refresh")) {
            chain.doFilter(request, response);
            return;
        }

        // Google / Kakao Callback 요청은 JWT 인증을 건너뜀
        if (requestUri.contains("/api/v1/auth/google/callback") || requestUri.contains("/api/v1/auth/kakao/callback")) {
            chain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 JWT Access Token 가져오기
        String token = resolveToken(request);
        if (token != null) {
            try {
                if (jwtUtil.validateToken(token, false)) {
                    Authentication authentication = jwtUtil.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    throw new JwtException("유효하지 않은 토큰");
                }
            } catch (ExpiredJwtException e) {
                sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED", "Access Token이 만료되었습니다. Refresh Token을 사용하세요.");
                return;
            } catch (JwtException e) {
                sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 Access Token입니다.");
                return;
            }
        }
        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
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


