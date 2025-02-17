package sweep.demo.Swagger.controller.auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sweep.demo.SocialType;
import sweep.demo.Swagger.dto.auth.SocialUserResponseDto;
import sweep.demo.global.security.JwtUtil;
import sweep.demo.service.auth.AuthService;
import sweep.demo.service.auth.GoogleAuthService;
import sweep.demo.service.auth.UserService;
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
        // 1. 인증 코드로 Access Token 받기
        String accessToken = googleAuthService.getAccessToken(code);
        // 2. Access Token으로 사용자 정보 가져오기
        Map<String, Object> googleUserInfo = googleAuthService.getUserInfo(accessToken);

        // 3. 사용자 정보 update&저장
        SocialUserResponseDto userResponse = authService.saveOrUpdateUser(googleUserInfo, accessToken, SocialType.GOOGLE);
        // 4. JWT 토큰 생성
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


