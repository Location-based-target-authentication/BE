package sweep.demo.Swagger.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sweep.demo.Swagger.dto.auth.SocialUserResponseDto;
import sweep.demo.Swagger.dto.auth.UserPhoneRequestDto;
import sweep.demo.global.security.JwtUtil;
import sweep.demo.service.auth.UserService;

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
        // 2. socialId 추출
        String socialId = jwtUtil.extractUserId(accessToken);
        System.out.println("현재 로그인한 사용자 ID: " + socialId);

        // 3. 전화번호 저장
        try {
            SocialUserResponseDto userResponse = userService.savePhoneNumber(socialId, phoneRequestDto.getPhoneNumber());
            return ResponseEntity.ok(Map.of(
                    "message", "전화번호가 성공적으로 저장됨",
                    "user", userResponse
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

}

