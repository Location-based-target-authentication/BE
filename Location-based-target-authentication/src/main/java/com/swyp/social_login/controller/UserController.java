package com.swyp.social_login.controller;

import com.swyp.global.security.JwtUtil;
import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.dto.UserPhoneRequestDto;
import com.swyp.social_login.service.auth.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    @Operation(summary = "전화번호 입력", description = "사용자가 전화번호를 입력하면 저장함")
    @PostMapping("/user/phone")
    public ResponseEntity<?> savePhoneNumber(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody UserPhoneRequestDto phoneRequestDto) {
        // 1. Access Token 검증
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authorization header가 없거나 유효하지 않음"));
        }
        String accessToken = authorizationHeader.substring(7);
        if (!jwtUtil.validateToken(accessToken, false)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid Access Token"));
        }
        // 2. userId 추출
        Long userId = jwtUtil.extractUserId(accessToken);
        // 3. 전화번호 저장
        try {
            SocialUserResponseDto userResponse = userService.savePhoneNumber(userId, phoneRequestDto.getPhoneNumber());
            return ResponseEntity.ok(Map.of(
                    "message", "전화번호가 성공적으로 저장됨",
                    "user", userResponse
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

}

