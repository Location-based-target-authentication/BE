package com.swyp.social_login.controller;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/google")
@RequiredArgsConstructor
@Tag(name = "Google Auth", description = "구글 소셜 로그인 API")
public class GoogleAuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @Operation(summary = "구글 로그인", description = "인가 코드를 받아서 JWT Access Token 반환")
    @PostMapping("/login")
    public ResponseEntity<Map<String, SocialUserResponseDto>> googleLogin(
            @RequestParam(name="code", required = false) String codeParam,
            @RequestBody(required = false) Map<String, String> body) {
        System.out.println("[GoogleAuthController] 로그인 시작");
        System.out.println("- codeParam: " + codeParam);
        System.out.println("- body: " + (body != null ? body.toString() : "null"));
        
        // body에서 code 또는 accessToken 추출
        String code = codeParam;
        String accessToken = null;
        
        if (body != null) {
            if (code == null) {
                code = body.get("code");
                System.out.println("[GoogleAuthController] Body에서 code 추출");
            }
            accessToken = body.get("accessToken");
            System.out.println("[GoogleAuthController] Body에서 accessToken 추출");
        }
        System.out.println("- 최종 code: " + code);
        
        // accessToken이 있는 경우 (모바일)
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                System.out.println("[GoogleAuthController] AccessToken으로 로그인 시도");
                Map<String, Object> googleUserInfo = googleAuthService.getUserInfo(accessToken);
                SocialUserResponseDto userResponse = authService.saveOrUpdateUser(googleUserInfo, accessToken, SocialType.GOOGLE);
                userResponse = authService.generateJwtTokens(userResponse);
                return ResponseEntity.ok(Map.of("data", userResponse));
            } catch (Exception e) {
                System.err.println("[GoogleAuthController] AccessToken 로그인 중 오류: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        }

        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("인가 코드(code)가 필요합니다.");
        }
        try{
            System.out.println("[GoogleAuthController] Access Token 요청 시작");
            // 기존의 accessToken 변수 재사용
            accessToken = googleAuthService.getAccessToken(code);
            System.out.println("[GoogleAuthController] Access Token 발급 성공: " + accessToken.substring(0, 10) + "...");
            
            System.out.println("[GoogleAuthController] 사용자 정보 요청 시작");
            Map<String, Object> googleUserInfo = googleAuthService.getUserInfo(accessToken);
            System.out.println("[GoogleAuthController] 사용자 정보 조회 성공: " + googleUserInfo);
            
            System.out.println("[GoogleAuthController] 사용자 저장/업데이트 시작");
            SocialUserResponseDto userResponse = authService.saveOrUpdateUser(googleUserInfo, accessToken, SocialType.GOOGLE);
            System.out.println("[GoogleAuthController] 사용자 저장/업데이트 성공: " + userResponse);
            
            System.out.println("[GoogleAuthController] JWT 토큰 생성 시작");
            userResponse = authService.generateJwtTokens(userResponse);
            System.out.println("[GoogleAuthController] JWT 토큰 생성 성공");
            
            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            System.out.println("[GoogleAuthController] 에러 발생: " + e.getMessage());
            System.out.println("[GoogleAuthController] 에러 타입: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    //access token으로 직접 넘겨서 테스트
    @Operation(summary = "구글 액세스 토큰으로 로그인", description = "모바일에서 받은 구글 액세스 토큰으로 로그인")
    @PostMapping("/userinfo")
    public ResponseEntity<Map<String, SocialUserResponseDto>> getGoogleUserInfo(
            @RequestParam(name = "accessToken") String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            Map<String, Object> googleUserInfo = googleAuthService.getUserInfo(accessToken);
            SocialUserResponseDto userResponse = authService.saveOrUpdateUser(googleUserInfo, accessToken, SocialType.GOOGLE);
            userResponse = authService.generateJwtTokens(userResponse);
            return ResponseEntity.ok(Map.of("data", userResponse));
        } catch (Exception e) {
            System.out.println("[GoogleAuthController] 모바일 로그인 에러: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "구글 로그인 후 callback", description = "구글 로그인 후 callback")
    @GetMapping("/callback")
    public ResponseEntity<String> googleCallback(@RequestParam(name="code") String code) {
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("유효하지 않은 인가 코드");
        }
        return ResponseEntity.ok("인가 코드가 정상적으로 전달되었음: " + code);
    }
}


