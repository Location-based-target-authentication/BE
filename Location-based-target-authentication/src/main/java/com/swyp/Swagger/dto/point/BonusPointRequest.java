package com.swyp.Swagger.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보너스 포인트 적립 요청")
public record BonusPointRequest(
    @Schema(description = "적립할 포인트", example = "100")
    Integer points,

    @Schema(description = "보너스 사유", example = "출석 보상")
    String reason
) {} 