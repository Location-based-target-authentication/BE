package com.swyp.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "목표 상세 정보 응답 DTO")
public class GoalDetailDto {

    @Schema(description = "목표 고유 번호", example = "1")
    private Long id;

    @Schema(description = "목표 이름", example = "매일 아침 조깅")
    private String name;

    @Schema(description = "목표 상태 (DRAFT, ACTIVE, COMPLETE)", example = "ACTIVE")
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "목표 시작일", example = "2025-01-01")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "목표 종료일", example = "2025-12-31")
    private LocalDate endDate;

    @Schema(description = "목표 장소명", example = "한강 공원")
    private String locationName;

    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;

    @Schema(description = "목표 수행 횟수", example = "30")
    private Integer targetCount;

    @Schema(description = "목표 수행 완료 횟수", example = "5")
    private Integer achievedCount;

    @Schema(description = "목표 수행 요일", example = "MON,TUE,THU")
    private String dayOfWeek;
}
