package com.swyp.social_login.controller;
import com.swyp.global.cache.AuthRequestCacheService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/kakao")
@Tag(name = "Kakao Auth", description = "카카오 소셜 로그인 API")
public class KakaoAuthController {
    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;
    private final AuthRequestCacheService cacheService;
    
    private static final Logger log = LoggerFactory.getLogger(KakaoAuthController.class);
    
    // 동시 진행 중인 요청을 추적하기 위한 맵
    private final Map<String, Long> processingRequests = new ConcurrentHashMap<>();
    
    @Operation(summary = "카카오 로그인", description = "인가 코드를 받아서 JWT Access Token 반환")
    @PostMapping("/login")
    public ResponseEntity<Map<String, SocialUserResponseDto>> kakaoLogin(
            @RequestParam(name = "code", required = false) String codeParam,
            @RequestBody(required = false) Map<String, String> body) {
        
        log.info("[KakaoAuthController] 로그인 요청 수신");
        // body에서 code 또는 accessToken 추출
        String code = codeParam;
        String accessToken = null;
        
        if (body != null) {
            if (code == null) {
                code = body.get("code");
                log.info("[KakaoAuthController] Body에서 code 추출");
            }
            accessToken = body.get("accessToken");
            log.info("[KakaoAuthController] Body에서 accessToken 추출");
        }
        
        // 동일한 코드로 이미 처리 중인 요청이 있는 경우 대기
        if (code != null && !code.isEmpty()) {
            if (processingRequests.containsKey(code)) {
                log.info("[KakaoAuthController] 동일한 인증 코드 {} 로 이미 요청이 처리 중입니다. 중복 요청을 방지합니다.", code);
                
                // 캐시에서 이 코드에 대한 토큰을 확인
                String cachedToken = cacheService.getAccessTokenByCode(code);
                if (cachedToken != null) {
                    // 이미 토큰이 있다면 이를 사용하여 사용자 정보 가져오기
                    try {
                        Map<String, Object> userInfo = kakaoAuthService.getUserInfo(cachedToken);
                        SocialUserResponseDto userResponse = authService.saveOrUpdateUser(userInfo, cachedToken, SocialType.KAKAO);
                        return ResponseEntity.ok(Map.of("data", userResponse));
                    } catch (Exception e) {
                        log.error("[KakaoAuthController] 캐시된 토큰으로 사용자 정보 조회 실패: {}", e.getMessage());
                        // 실패 시 계속 진행하여 새 토큰 발급 시도
                    }
                }
            } else {
                // 처리 중인 요청 목록에 추가
                processingRequests.put(code, System.currentTimeMillis());
            }
        }
        
        try {
            // accessToken이 있는 경우 (모바일)
            if (accessToken != null && !accessToken.isEmpty()) {
                try {
                    log.info("[KakaoAuthController] AccessToken으로 로그인 시도");
                    Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
                    SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
                    userResponse = authService.generateJwtTokens(userResponse);
                    return ResponseEntity.ok(Map.of("data", userResponse));
                } catch (Exception e) {
                    log.error("[KakaoAuthController] AccessToken 로그인 중 오류: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }
            }

            if (code == null || code.isEmpty()) {
                log.error("[KakaoAuthController] 인가 코드 누락");
                throw new IllegalArgumentException("인가 코드(code)가 필요합니다.");
            }

            log.info("[KakaoAuthController] 코드로 로그인 처리 시작");
            SocialUserResponseDto userResponse = authService.loginWithKakao(code);
            log.info("[KakaoAuthController] 로그인 성공: userId={}", userResponse.getUserId());
            
            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            log.error("[KakaoAuthController] 로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } finally {
            // 처리 중인 요청 목록에서 제거
            if (code != null) {
                processingRequests.remove(code);
            }
        }
    }
    
    @Operation(summary = "카카오 사용자 정보 조회", description = "액세스 토큰으로 카카오 사용자 정보 조회")
    @PostMapping("/userinfo")
    public ResponseEntity<Map<String, SocialUserResponseDto>> getKakaoUserInfo(@RequestParam(name = "accessToken") String accessToken) {
        log.info("[KakaoAuthController] 사용자 정보 조회 요청 수신");
        
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("[KakaoAuthController] Access Token 누락");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            log.info("[KakaoAuthController] 카카오 사용자 정보 요청 시작");
            Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
            log.info("[KakaoAuthController] 카카오 사용자 정보 조회 성공");

            String socialId = kakaoUserInfo.getOrDefault("socialId", "").toString();
            // 사용자 ID로 캐싱
            if (!socialId.isEmpty()) {
                cacheService.cacheAccessTokenByUserId(socialId, accessToken);
            }

            log.info("[KakaoAuthController] 사용자 정보 저장/업데이트 시작");
            SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
            log.info("[KakaoAuthController] 사용자 정보 저장/업데이트 성공");

            log.info("[KakaoAuthController] JWT 토큰 생성 시작");
            userResponse = authService.generateJwtTokens(userResponse);
            log.info("[KakaoAuthController] JWT 토큰 생성 성공");

            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            log.error("[KakaoAuthController] 사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Operation(summary = "카카오 로그인 후 callback", description = "카카오 로그인 후 callback")
    @GetMapping("/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam(name="code") String code) {
        log.info("[KakaoAuthController] 콜백 요청 수신");
        
        if (code == null || code.isEmpty()) {
            log.error("[KakaoAuthController] 콜백 - 인가 코드 누락");
            return ResponseEntity.badRequest().body("유효하지 않은 인가 코드");
        }

        log.info("[KakaoAuthController] 콜백 - 인가 코드 수신 성공: {}", code);
        return ResponseEntity.ok("인가 코드가 정상적으로 전달되었음: " + code);
    }
}


