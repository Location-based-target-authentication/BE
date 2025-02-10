package com.swyp.Swagger.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 요청")
public record SocialLoginRequest(
    @Schema(description = "소셜 인증 코드", example = "4/P7q7W91a-oMsCeLvIaQm6bTrgtp7")
    String code
) {} 