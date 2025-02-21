package com.swyp.point.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//(포인트 TEST용)
public class GoalCreation {

    private Long userId;              // 사용자 ID
    private String name;              // 목표 이름
    private String status;            // 목표 상태 (DRAFT, ACTIVE)
    private LocalDate startDate;      // 시작일
    private LocalDate endDate;        // 종료일
    private List<String> selectedDays; // 선택된 요일
    private Integer points;

}

