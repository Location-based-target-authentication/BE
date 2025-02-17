package com.swyp.Swagger.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "목표 달성 포인트 적립 요청")
public record GoalAchievedRequest(
    @Schema(description = "목표 ID", example = "1")
    Long goalId,

    @Schema(description = "적립할 포인트", example = "100")
    Integer points
) {} 