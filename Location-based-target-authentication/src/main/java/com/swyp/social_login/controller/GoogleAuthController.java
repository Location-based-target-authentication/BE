package com.swyp.social_login.controller;
import com.swyp.global.cache.AuthRequestCacheService;
import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.service.auth.AuthService;
import com.swyp.social_login.service.auth.GoogleAuthService;
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
@RequestMapping("/api/v1/auth/google")
@RequiredArgsConstructor
@Tag(name = "Google Auth", description = "구글 소셜 로그인 API")
public class GoogleAuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final AuthRequestCacheService cacheService;
    
    private static final Logger log = LoggerFactory.getLogger(GoogleAuthController.class);
    
    // 동시 진행 중인 요청을 추적하기 위한 맵
    private final Map<String, Long> processingRequests = new ConcurrentHashMap<>();

    @Operation(summary = "구글 로그인", description = "인가 코드를 받아서 JWT Access Token 반환")
    @PostMapping("/login")
    public ResponseEntity<Map<String, SocialUserResponseDto>> googleLogin(
            @RequestParam(name="code", required = false) String codeParam,
            @RequestBody(required = false) Map<String, String> body) {
        
        log.info("[GoogleAuthController] 로그인 시작");
        
        // body에서 code 또는 accessToken 추출
        String code = codeParam;
        String accessToken = null;
        
        if (body != null) {
            if (code == null) {
                code = body.get("code");
                log.info("[GoogleAuthController] Body에서 code 추출: {}", code);
            }
            accessToken = body.get("accessToken");
            log.info("[GoogleAuthController] Body에서 accessToken 추출");
        }
        
        // 동일한 코드로 이미 처리 중인 요청이 있는 경우 대기
        if (code != null && !code.isEmpty()) {
            if (processingRequests.containsKey(code)) {
                log.info("[GoogleAuthController] 동일한 인증 코드 {} 로 이미 요청이 처리 중입니다. 중복 요청을 방지합니다.", code);
                
                // 캐시에서 이 코드에 대한 토큰을 확인
                String cachedToken = cacheService.getAccessTokenByCode(code);
                if (cachedToken != null) {
                    // 이미 토큰이 있다면 이를 사용하여 사용자 정보 가져오기
                    try {
                        Map<String, Object> userInfo = googleAuthService.getUserInfo(cachedToken);
                        SocialUserResponseDto userResponse = authService.saveOrUpdateUser(userInfo, cachedToken, SocialType.GOOGLE);
                        return ResponseEntity.ok(Map.of("data", userResponse));
                    } catch (Exception e) {
                        log.error("[GoogleAuthController] 캐시된 토큰으로 사용자 정보 조회 실패: {}", e.getMessage());
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
                    log.info("[GoogleAuthController] AccessToken으로 로그인 시도");
                    Map<String, Object> googleUserInfo = googleAuthService.getUserInfo(accessToken);
                    SocialUserResponseDto userResponse = authService.saveOrUpdateUser(googleUserInfo, accessToken, SocialType.GOOGLE);
                    userResponse = authService.generateJwtTokens(userResponse);
                    return ResponseEntity.ok(Map.of("data", userResponse));
                } catch (Exception e) {
                    log.error("[GoogleAuthController] AccessToken 로그인 중 오류: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }
            }

            if (code == null || code.isEmpty()) {
                log.error("[GoogleAuthController] 인가 코드 누락");
                throw new IllegalArgumentException("인가 코드(code)가 필요합니다.");
            }
            
            log.info("[GoogleAuthController] 코드로 로그인 처리 시작");
            SocialUserResponseDto userResponse = authService.loginWithGoogle(code);
            log.info("[GoogleAuthController] 로그인 성공: userId={}", userResponse.getUserId());
            
            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            log.error("[GoogleAuthController] 로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } finally {
            // 처리 중인 요청 목록에서 제거
            if (code != null) {
                processingRequests.remove(code);
            }
        }
    }

    @Operation(summary = "구글 액세스 토큰으로 로그인", description = "모바일에서 받은 구글 액세스 토큰으로 로그인")
    @PostMapping("/userinfo")
    public ResponseEntity<Map<String, SocialUserResponseDto>> getGoogleUserInfo(
            @RequestParam(name = "accessToken") String accessToken) {
        log.info("[GoogleAuthController] 사용자 정보 조회 요청 수신");
        
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("[GoogleAuthController] Access Token 누락");
            return ResponseEntity.badRequest().body(null);
        }
        
        try {
            log.info("[GoogleAuthController] 구글 사용자 정보 요청 시작");
            Map<String, Object> googleUserInfo = googleAuthService.getUserInfo(accessToken);
            log.info("[GoogleAuthController] 구글 사용자 정보 조회 성공");

            String socialId = googleUserInfo.getOrDefault("socialId", "").toString();
            // 사용자 ID로 캐싱
            if (!socialId.isEmpty()) {
                cacheService.cacheAccessTokenByUserId(socialId, accessToken);
            }

            log.info("[GoogleAuthController] 사용자 정보 저장/업데이트 시작");
            SocialUserResponseDto userResponse = authService.saveOrUpdateUser(googleUserInfo, accessToken, SocialType.GOOGLE);
            log.info("[GoogleAuthController] 사용자 정보 저장/업데이트 성공");

            log.info("[GoogleAuthController] JWT 토큰 생성 시작");
            userResponse = authService.generateJwtTokens(userResponse);
            log.info("[GoogleAuthController] JWT 토큰 생성 성공");

            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            log.error("[GoogleAuthController] 사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "구글 로그인 후 callback", description = "구글 로그인 후 callback")
    @GetMapping("/callback")
    public ResponseEntity<String> googleCallback(@RequestParam(name="code") String code) {
        log.info("[GoogleAuthController] 콜백 요청 수신");
        
        if (code == null || code.isEmpty()) {
            log.error("[GoogleAuthController] 콜백 - 인가 코드 누락");
            return ResponseEntity.badRequest().body("유효하지 않은 인가 코드");
        }

        log.info("[GoogleAuthController] 콜백 - 인가 코드 수신 성공: {}", code);
        return ResponseEntity.ok("인가 코드가 정상적으로 전달되었음: " + code);
    }
}


