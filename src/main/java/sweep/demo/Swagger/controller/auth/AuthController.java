package sweep.demo.Swagger.controller.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import sweep.demo.Swagger.dto.auth.SocialUserResponseDto;
import sweep.demo.entity.auth.AuthUser;
import sweep.demo.exception.RefreshTokenExpiredException;
import sweep.demo.global.security.JwtUtil;
import sweep.demo.repository.auth.UserRepository;
import sweep.demo.service.auth.UserService;
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
        // 1. Authorization 헤더에서 Refresh Token 추출
        if (refreshTokenHeader == null || !refreshTokenHeader.startsWith("Bearer ")) {
            System.out.println("[AuthController] refresh token 없음!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh Token이 필요함"));
        }
        String refreshToken = refreshTokenHeader.substring(7); // "Bearer " 이후의 실제 토큰 값
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

        String socialId = jwtUtil.extractUserId(refreshToken);
        if (socialId == null || socialId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Refresh Token"));
        }

        // 4. DB에서 해당 사용자 조회
        Optional<AuthUser> optionalUser = userRepository.findBySocialId(socialId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
        AuthUser user = optionalUser.get();
        // 5. 새로운 Access Token 생성 및 반환
        String newAccessToken = jwtUtil.generateAccessToken(user.getSocialId());
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

        // 2. 토큰 검증
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
        String socialId = jwtUtil.extractUserId(accessToken);
        if (socialId == null || socialId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Access Token"));
        }
        // DB에서 사용자 정보 조회
        SocialUserResponseDto userResponse = userService.getUserInfoFromDb(socialId);
        if (userResponse == null) {
            System.out.println("[AuthController] 사용자를 찾을 수 없음");
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
                "phoneNumber", phoneNumber
        ));
    }
}
