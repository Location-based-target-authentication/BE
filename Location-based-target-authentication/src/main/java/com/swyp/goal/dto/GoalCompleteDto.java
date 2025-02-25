package com.swyp.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "완료된 목표 정보 Dto")
public class GoalCompleteDto {

	@Schema(description = "목표 이름", example = "매일 아침 조깅")
	private String name; //목표 이름
	
	@Schema(description = "목표 수행 횟수", example = "30")
	private Integer targetCount; //목표 수행 횟수
	
	@Schema(description = "목표 수행 완료 횟수", example = "5")
	private Integer achievedCount;//실제 수행 횟수
	
	@Schema(description = "총 획득 포인트", example = "5000")
	private Integer totalPointsEarned;//총 획득 포인트
	
	@Schema(description = "목표 시작일", example = "2025-01-01")
	private LocalDate startDate;//목표 시작일
	
	@Schema(description = "목표 종료일", example = "2025-12-31")
	private LocalDate endDate;//목표 종료일
	
	@Schema(description = "목표 설정 요일", example = "MON,TUE,THU")
	private String dayOfWeek;//설정 요일
}
