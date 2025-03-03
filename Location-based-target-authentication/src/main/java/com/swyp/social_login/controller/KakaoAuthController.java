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
    private final KakaoAuthService kakaoAuthService;
    @Operation(summary = "카카오 로그인", description = "인가 코드를 받아서 JWT Access Token 반환")
    @PostMapping("/login")
    public ResponseEntity<Map<String, SocialUserResponseDto>> kakaoLogin(
            @RequestParam(name = "code", required = false) String codeParam,
            @RequestBody(required = false) Map<String, String> body) {
        
        System.out.println("[KakaoAuthController] 로그인 요청 수신");
        // body에서 code 또는 accessToken 추출
        String code = codeParam;
        String accessToken = null;
        
        if (body != null) {
            if (code == null) {
                code = body.get("code");
                System.out.println("[KakaoAuthController] Body에서 code 추출");
            }
            accessToken = body.get("accessToken");
            System.out.println("[KakaoAuthController] Body에서 accessToken 추출");
        }
        
        // accessToken이 있는 경우 (모바일)
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                System.out.println("[KakaoAuthController] AccessToken으로 로그인 시도");
                Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
                SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
                userResponse = authService.generateJwtTokens(userResponse);
                return ResponseEntity.ok(Map.of("data", userResponse));
            } catch (Exception e) {
                System.err.println("[KakaoAuthController] AccessToken 로그인 중 오류: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        }

        if (code == null || code.isEmpty()) {
            System.err.println("[KakaoAuthController] 인가 코드 누락");
            throw new IllegalArgumentException("인가 코드(code)가 필요합니다.");
        }

        try {
            System.out.println("[KakaoAuthController] Access Token 요청 시작");
            // 기존의 accessToken 변수 재사용
            accessToken = kakaoAuthService.getAccessToken(code);
            System.out.println("[KakaoAuthController] Access Token 발급 성공");

            System.out.println("[KakaoAuthController] 사용자 정보 요청 시작");
            Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
            System.out.println("[KakaoAuthController] 사용자 정보 조회 성공");
            
            System.out.println("[KakaoAuthController] 사용자 정보 저장/업데이트 시작");
            SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
            System.out.println("[KakaoAuthController] 사용자 정보 저장/업데이트 성공");

            System.out.println("[KakaoAuthController] JWT 토큰 생성 시작");
            userResponse = authService.generateJwtTokens(userResponse);
            System.out.println("[KakaoAuthController] JWT 토큰 생성 성공");

            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            System.err.println("[KakaoAuthController] 로그인 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
    //access token을 직접 넘겨서 테스트함
    @PostMapping("/userinfo")
    public ResponseEntity<Map<String, SocialUserResponseDto>> getKakaoUserInfo(@RequestParam(name = "accessToken") String accessToken) {
        System.out.println("[KakaoAuthController] 사용자 정보 조회 요청 수신");
        
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("[KakaoAuthController] Access Token 누락");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            System.out.println("[KakaoAuthController] 카카오 사용자 정보 요청 시작");
            Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
            System.out.println("[KakaoAuthController] 카카오 사용자 정보 조회 성공");

            System.out.println("[KakaoAuthController] 사용자 정보 저장/업데이트 시작");
            SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
            System.out.println("[KakaoAuthController] 사용자 정보 저장/업데이트 성공");

            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            System.err.println("[KakaoAuthController] 사용자 정보 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @Operation(summary = "카카오 로그인 후 callback", description = "카카오 로그인 후 callback")
    @GetMapping("/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam(name="code") String code) {
        System.out.println("[KakaoAuthController] 콜백 요청 수신");
        
        if (code == null || code.isEmpty()) {
            System.err.println("[KakaoAuthController] 콜백 - 인가 코드 누락");
            return ResponseEntity.badRequest().body("유효하지 않은 인가 코드");
        }

        System.out.println("[KakaoAuthController] 콜백 - 인가 코드 수신 성공: " + code);
        return ResponseEntity.ok("인가 코드가 정상적으로 전달되었음: " + code);
    }
}


