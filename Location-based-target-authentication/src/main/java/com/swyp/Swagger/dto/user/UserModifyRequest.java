package com.swyp.Swagger.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 수정 요청")
public record UserModifyRequest(
    @Schema(description = "이름", example = "홍길동")
    String username,

    @Schema(description = "자동 로그인 여부", example = "true")
    Boolean autoLogin
) {} 