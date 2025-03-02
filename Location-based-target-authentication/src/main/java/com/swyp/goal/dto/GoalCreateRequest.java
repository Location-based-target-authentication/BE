package com.swyp.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.swyp.goal.entity.DayOfWeek;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoalCreateRequest {
    private Long userId; // 사용자 ID
    private String name; // 목표 이름
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate; // 시작일
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")	
    private LocalDate endDate; // 종료일
    
    private String locationName; // 장소 이름
    private BigDecimal latitude; // 위도
    private BigDecimal longitude; //	 경도
    private String status; // 상태 (DRAFT, ACTIVE)
    private List<DayOfWeek> selectedDays; // 선택된 요일
}