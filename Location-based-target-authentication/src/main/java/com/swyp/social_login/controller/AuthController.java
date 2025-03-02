package com.swyp.social_login.controller;

import com.swyp.exception.RefreshTokenExpiredException;
import com.swyp.global.security.JwtUtil;
import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import com.swyp.social_login.service.auth.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "공통 사용자 조회 API")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        if (refreshTokenHeader == null || !refreshTokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh Token이 필요함"));
        }
        String refreshToken = refreshTokenHeader.substring(7);
        // Refresh Token 검증 시 isRefreshToken = true 설정
        try {
            jwtUtil.validateToken(refreshToken, true);
        } catch (RefreshTokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "REFRESH_TOKEN_EXPIRED",
                    "message", "Refresh Token이 만료되었습니다. 다시 로그인하세요."
            ));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "INVALID_TOKEN",
                    "message", "유효하지 않은 Refresh Token입니다."
            ));
        }

        Long userId = jwtUtil.extractUserId(refreshToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Refresh Token"));
        }

        // 4. DB에서 해당 사용자 조회
        Optional<AuthUser> optionalUser = userRepository.findByUserId(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
        AuthUser user = optionalUser.get();
        // 5. 새로운 Access Token 생성 및 반환
        String newAccessToken = jwtUtil.generateAccessToken(user.getUserId());
        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "tokenType", "Bearer"
        ));
    }

    @Operation(summary = "사용자 정보 조회", description = "JWT Access Token을 사용하여 카카오/구글 사용자 정보를 가져옴")
    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        // 1. Access Token 추출
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authorization header가 없거나 유효하지 않음"));
        }
        String accessToken = authorizationHeader.substring(7);
        // 토큰 검증
        if (!jwtUtil.validateToken(accessToken, false)) {
            // 만료된 토큰인지 확인
            try {
                jwtUtil.validateToken(accessToken, false);
            } catch (ExpiredJwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "TOKEN_EXPIRED",
                        "message", "Access Token이 만료되었습니다. Refresh Token을 사용하세요."
                ));
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "INVALID_TOKEN",
                        "message", "유효하지 않은 Access Token입니다. 다시 로그인하세요."
                ));
            }
        }

        // JWT에서 socialId 추출
        Long userId = jwtUtil.extractUserId(accessToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Access Token"));
        }
        // DB에서 사용자 정보 조회
        SocialUserResponseDto userResponse = userService.getUserInfoFromDb(userId);
        if (userResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        // 필드 값이 null인지 확인하고 처리
        String username = (userResponse.getUsername() != null) ? userResponse.getUsername() : "Unknown";
        String email = (userResponse.getEmail() != null) ? userResponse.getEmail() : "Unknown";
        String phoneNumber = (userResponse.getPhoneNumber() != null) ? userResponse.getPhoneNumber() : "Unknown";

        // 응답 반환
        return ResponseEntity.ok(Map.of(
                "username", username,
                "email", email,
                "phoneNumber", phoneNumber,
                "socialType", userResponse.getSocialType().name()
        ));
    }
}
