package com.swyp.Swagger.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자동 로그인 설정 요청")
public record AutoLoginRequest(
    @Schema(description = "자동 로그인 사용 여부", example = "true")
    Boolean enabled
) {} 