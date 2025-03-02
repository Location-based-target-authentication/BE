package com.swyp.goal.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "목표 홈 화면 응답 DTO")
public class GoalHomeResponseDto {

    @Schema(description = "목표 이름", example = "매일 30분 독서")
    private String goalName;
    	
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "목표 시작일", example = "2025-01-01")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "목표 종료일", example = "2025-12-31")
    private LocalDate endDate;

    @Schema(description = "목표 상태 (DRAFT: 임시저장, ACTIVE: 진행중, COMPLETE: 완료)", example = "ACTIVE")
    private String status;

    @Schema(description = "오늘 목표 인증 여부 (true: 인증 완료, false: 인증 미완료)", example = "true")
    private boolean isAchievedToday;
    
    @Schema(description = "목표 설정 요일", example = "MON,TUE,THU")
	private String dayOfWeek;//설정 요일
}
