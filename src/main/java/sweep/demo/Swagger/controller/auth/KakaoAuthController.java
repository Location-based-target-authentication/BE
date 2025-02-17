package sweep.demo.Swagger.controller.auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sweep.demo.SocialType;
import sweep.demo.Swagger.dto.auth.SocialUserResponseDto;
import sweep.demo.entity.auth.AuthUser;
import sweep.demo.global.security.JwtUtil;
import sweep.demo.repository.auth.UserRepository;
import sweep.demo.service.auth.AuthService;
import sweep.demo.service.auth.KakaoAuthService;
import sweep.demo.service.auth.UserService;
import sweep.demo.service.user.UserServiceImpl;

import java.util.HashMap;
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
    public ResponseEntity<SocialUserResponseDto> kakaoLogin(@RequestParam(name="code") String code) {
        String accessToken = kakaoAuthService.getAccessToken(code);
        Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
        String kakaoId = kakaoUserInfo.get("socialId").toString();
        // 3. 사용자 정보 update / 저장
        SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
        // 4. JWT 토큰 생성
        userResponse = authService.generateJwtTokens(userResponse);
        return ResponseEntity.ok(userResponse);
    }
    //access token을 직접 넘겨서 테스트함
    @PostMapping("/userinfo")
    public ResponseEntity<SocialUserResponseDto> getKakaoUserInfo(@RequestParam(name = "accessToken") String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        Map<String, Object> kakaoUserInfo = kakaoAuthService.getUserInfo(accessToken);
        SocialUserResponseDto userResponse = authService.saveOrUpdateUser(kakaoUserInfo, accessToken, SocialType.KAKAO);
        return ResponseEntity.ok(userResponse);
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


