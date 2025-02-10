package com.swyp.Swagger.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 차감 요청")
public record PointDeductRequest(
    @Schema(description = "차감할 포인트", example = "100")
    Integer points,

    @Schema(description = "목표 ID", example = "1")
    Long goalId
) {} 