package com.swyp.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class GoalUpdateDto {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer radius;
}