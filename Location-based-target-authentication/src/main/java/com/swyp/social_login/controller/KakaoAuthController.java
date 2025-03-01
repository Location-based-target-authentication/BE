package com.swyp.social_login.controller;
import com.swyp.global.security.JwtUtil;
import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.service.auth.AuthService;
import com.swyp.social_login.service.auth.KakaoAuthService;
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
@RequestMapping("/api/v1/auth/kakao")
@Tag(name = "Kakao Auth", description = "카카오 소셜 로그인 API")
public class KakaoAuthController {
    private final AuthService authService;
    private final UserService userService;
    private final KakaoAuthService kakaoAuthService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "카카오 로그인", description = "인가 코드를 받아서 JWT Access Token 반환")
    @PostMapping("/login")
    public ResponseEntity<Map<String, SocialUserResponseDto>> kakaoLogin(
            @RequestParam(name = "code", required = false) String codeParam,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "Referer", required = false) String referer) {
        
        String code = codeParam;
        if (code == null && body != null) {
            code = body.get("code");
        }
        
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("인가 코드(code)가 필요합니다.");
        }

        try {
            String accessToken = kakaoAuthService.getAccessToken(code, referer);
            Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
            
            SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
            userResponse = authService.generateJwtTokens(userResponse);
            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            e.printStackTrace(); // 로깅 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
    //access token을 직접 넘겨서 테스트함
    @PostMapping("/userinfo")
    public ResponseEntity<Map<String, SocialUserResponseDto>> getKakaoUserInfo(@RequestParam(name = "accessToken") String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
        SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
        return ResponseEntity.ok(Map.of("data", userResponse));
    }
    @Operation(summary = "카카오 로그인 후 callback", description = "카카오 로그인 후 callback")
    @GetMapping("/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam(name="code") String code) {
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 인가 코드");
        }
        return ResponseEntity.ok("인가 코드가 정상적으로 전달되었음: " + code);
    }
}


