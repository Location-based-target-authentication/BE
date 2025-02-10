package com.swyp.Swagger.controller;

import com.swyp.Swagger.dto.auth.AuthResponse;
import com.swyp.Swagger.dto.auth.SocialLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "소셜 로그인 관련 API")
@RestController
public class AuthSwaggerController {

    @Operation(
        summary = "구글 로그인",
        description = "구글 소셜 로그인을 진행합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))
            )
        }
    )
    @PostMapping("/api/v1/auth/login/google")
    public AuthResponse googleLogin(
        @RequestBody @Schema(description = "구글 인증 정보") SocialLoginRequest request
    ) {
        return null;
    }

    @Operation(
        summary = "카카오 로그인",
        description = "카카오 소셜 로그인을 진행합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))
            )
        }
    )
    @PostMapping("/api/b1/auth/login/kakao")
    public AuthResponse kakaoLogin(
        @RequestBody @Schema(description = "카카오 인증 정보") SocialLoginRequest request
    ) {
        return null;
    }
} 