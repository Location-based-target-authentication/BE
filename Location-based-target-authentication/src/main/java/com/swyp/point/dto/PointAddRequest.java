package com.swyp.point.dto;


import com.swyp.goal.entity.Goal;
import com.swyp.point.enums.PointType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointAddRequest {
    private int points; // 적립할 포인트 (예: 50, 60, 2000)
    private PointType pointType;
    private String description;
    private Long goalId;

}
