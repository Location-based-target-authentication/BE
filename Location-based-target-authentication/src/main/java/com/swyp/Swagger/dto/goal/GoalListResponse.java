package com.swyp.Swagger.dto.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "전체 목표 목록 응답")
public record GoalListResponse(
    @Schema(description = "전체 목표 수", example = "10")
    Integer totalCount,

    @Schema(description = "목표 목록")
    List<GoalSummary> goals
) {}

@Schema(description = "목표 요약 정보")
record GoalSummary(
    @Schema(description = "목표 ID", example = "1")
    Long goalId,

    @Schema(description = "목표 제목", example = "아침 6시 기상")
    String title,

    @Schema(description = "목표 상태", example = "ACTIVE", 
           allowableValues = {"DRAFT", "ACTIVE", "COMPLETED", "FAILED"})
    String status,

    @Schema(description = "달성률(%)", example = "50")
    Integer achievementRate
) {} 