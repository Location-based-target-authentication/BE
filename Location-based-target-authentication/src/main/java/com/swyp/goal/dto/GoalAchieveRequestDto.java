package com.swyp.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoalAchieveRequestDto {
    private Long userId;
    private Double latitude;
    private Double longitude;
}
