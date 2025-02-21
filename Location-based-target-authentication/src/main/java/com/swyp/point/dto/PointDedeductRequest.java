package com.swyp.point.dto;


import com.swyp.goal.entity.Goal;
import com.swyp.point.enums.PointType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointDedeductRequest {
    private int points;
    private PointType pointType;
    private String description;
    private Long goalId;
}
