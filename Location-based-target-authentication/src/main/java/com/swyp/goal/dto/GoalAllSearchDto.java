package com.swyp.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.swyp.goal.entity.GoalStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전체 목표 조회 Dto")
public class GoalAllSearchDto {

    @Schema(description = "목표 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "101")
    private Long userId;

    @Schema(description = "목표 이름", example = "매일 아침 조깅")
    private String name;

    @Schema(description = "목표 상태 (DRAFT, ACTIVE, COMPLETE)", example = "ACTIVE")
    private GoalStatus status;

    @Schema(description = "목표 시작 날짜", example = "2025-03-01")
    private LocalDate startDate;

    @Schema(description = "목표 종료 날짜", example = "2025-06-01")
    private LocalDate endDate;

    @Schema(description = "목표 장소 이름", example = "한강공원")
    private String locationName;

    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;

    @Schema(description = "목표 반경 (단위: m)", example = "100")
    private Integer radius;

    @Schema(description = "목표 설정 횟수", example = "10")
    private Integer targetCount;

    @Schema(description = "목표 달성 횟수", example = "3")
    private Integer achievedCount;
    
    @Schema(description = "목표 달력 인증 여부 (날짜 + 인증 여부)", 
            example = "[{\"date\":\"2025-03-01\",\"verified\":true}, {\"date\":\"2025-03-02\",\"verified\":true}]")
    private List<GoalDateDto> dateAuthentication;
    
    @Schema(description = "목표 달력을 위한 전체 날짜값(today가 startDate의 주에 속하면 이번 주 + 다음 주 ,  today가 startDate의 주에 속하지 않으면 지난주 + 이번 주) ", example = "[2025-03-01,2025-03-02,2025-03-03]")
    private List<LocalDate> calender;
    
    @Schema(description = "목표 설정 요일", example = "MON,TUE,THU")
	private String dayOfWeek;//설정 요일
    
}

