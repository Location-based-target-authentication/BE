package com.swyp.Swagger.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답")
public record AuthResponse(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJ...")
    String accessToken,

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJ...")
    String refreshToken,

    @Schema(description = "사용자 ID", example = "1")
    Long userId
) {} 