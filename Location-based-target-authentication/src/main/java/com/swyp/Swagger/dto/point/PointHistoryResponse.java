package com.swyp.Swagger.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 이력 응답")
public record PointHistoryResponse(
    @Schema(description = "포인트 이력 ID", example = "1")
    Long id,

    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "변동 포인트", example = "100")
    Integer points,

    @Schema(description = "변동 유형", example = "ACHIEVEMENT")
    String type,

    @Schema(description = "변동 사유", example = "목표 달성 보상")
    String description,

    @Schema(description = "관련 목표 ID", example = "1")
    Long relatedGoalId,

    @Schema(description = "변동 일시", example = "2024-02-09T15:30:00")
    String createdAt
) {} 