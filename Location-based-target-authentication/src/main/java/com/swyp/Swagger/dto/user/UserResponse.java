package com.swyp.Swagger.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 응답")
public record UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long id,

    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "이름", example = "홍길동")
    String username,

    @Schema(description = "소셜 로그인 타입", example = "GOOGLE")
    String socialType,

    @Schema(description = "자동 로그인 여부", example = "true")
    Boolean autoLogin,

    @Schema(description = "생성일시", example = "2024-02-09T15:30:00")
    String createdAt
) {} 