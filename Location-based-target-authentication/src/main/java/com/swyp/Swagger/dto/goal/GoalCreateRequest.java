package com.swyp.Swagger.dto.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "목표 생성 요청")
public record GoalCreateRequest(
    @Schema(description = "목표 이름", example = "아침 운동하기")
    String name,

    @Schema(description = "시작일", example = "2024-02-09")
    String startDate,

    @Schema(description = "장소명", example = "헬스장")
    String locationName,

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude,

    @Schema(description = "인증 반경", example = "100")
    Integer radius,

    @Schema(description = "반복 요일", example = "[\"MON\", \"WED\", \"FRI\"]")
    List<String> dayOfWeek
) {} 