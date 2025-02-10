package com.swyp.Swagger.dto.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "목표 달성 인증 응답")
public record GoalAchievementResponse(
    @Schema(description = "목표 ID", example = "1")
    Long goalId,

    @Schema(description = "인증 성공 여부", example = "true")
    Boolean success,

    @Schema(description = "획득한 포인트", example = "100")
    Integer earnedPoints,

    @Schema(description = "달성률(%)", example = "100")
    Integer achievementRate,

    @Schema(description = "인증 시간", example = "2024-02-09T15:30:00")
    LocalDateTime achievedAt
) {} 