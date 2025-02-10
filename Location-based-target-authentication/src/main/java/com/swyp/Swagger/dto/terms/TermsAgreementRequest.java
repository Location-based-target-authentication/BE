package com.swyp.Swagger.dto.terms;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "약관 동의 요청")
public record TermsAgreementRequest(
    @Schema(description = "약관 ID", example = "1")
    Long termsId,

    @Schema(description = "동의 여부", example = "true")
    Boolean agreed
) {} 