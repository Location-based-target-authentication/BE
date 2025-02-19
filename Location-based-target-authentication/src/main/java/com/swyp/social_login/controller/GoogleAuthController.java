package com.swyp.social_login.controller;
import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.service.auth.AuthService;
import com.swyp.social_login.service.auth.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<SocialUserResponseDto> googleLogin(@RequestParam(name="code") String code) {
        String accessToken = googleAuthService.getAccessToken(code);
        //  Access Token으로 사용자 정보 가져오기
        Map<String, Object> googleUserInfo = googleAuthService.getUserInfo(accessToken);
        SocialUserResponseDto userResponse = authService.saveOrUpdateUser(googleUserInfo, accessToken, SocialType.GOOGLE);
        // JWT 토큰 생성
        userResponse = authService.generateJwtTokens(userResponse);
        return ResponseEntity.ok(userResponse);
    }
    //access token으로 직접 넘겨서 테스트
    @PostMapping("/userinfo")
    public ResponseEntity<SocialUserResponseDto> getGoogleUserInfo(@RequestParam(name = "accessToken") String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        Map<String, Object> googleUserInfo = googleAuthService.getUserInfo(accessToken);
        SocialUserResponseDto userResponse = authService.saveOrUpdateUser(googleUserInfo, accessToken, SocialType.GOOGLE);
        return ResponseEntity.ok(userResponse);
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


