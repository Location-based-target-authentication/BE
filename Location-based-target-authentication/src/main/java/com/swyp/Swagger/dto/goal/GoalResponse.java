package com.swyp.Swagger.dto.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "목표 응답")
public record GoalResponse(
    @Schema(description = "목표 ID", example = "1")
    Long goalId,

    @Schema(description = "목표 제목", example = "아침 6시 기상")
    String title,

    @Schema(description = "목표 내용", example = "매일 아침 6시에 일어나서 공부하기")
    String content,

    @Schema(description = "목표 상태", example = "ACTIVE", 
           allowableValues = {"DRAFT", "ACTIVE", "COMPLETED", "FAILED"})
    String status,

    @Schema(description = "목표 장소", example = "스타벅스 강남점")
    String location,

    @Schema(description = "목표 위도", example = "37.5665")
    Double latitude,

    @Schema(description = "목표 경도", example = "126.9780")
    Double longitude,

    @Schema(description = "시작 시간", example = "2024-02-09T06:00:00")
    LocalDateTime startAt,

    @Schema(description = "종료 시간", example = "2024-02-09T22:00:00")
    LocalDateTime endAt,

    @Schema(description = "달성률(%)", example = "50")
    Integer achievementRate,

    @Schema(description = "생성 시간", example = "2024-02-09T15:30:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 시간", example = "2024-02-09T15:30:00")
    LocalDateTime updatedAt
) {} 