package com.swyp.Swagger.dto.terms;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "약관 응답")
public record TermsResponse(
    @Schema(description = "약관 ID", example = "1")
    Long id,

    @Schema(description = "약관 유형", example = "SERVICE")
    String type,

    @Schema(description = "약관 내용", example = "서비스 이용약관 내용...")
    String content,

    @Schema(description = "약관 버전", example = "1.0")
    String version,

    @Schema(description = "생성일시", example = "2024-02-09T15:30:00")
    String createdAt
) {} 