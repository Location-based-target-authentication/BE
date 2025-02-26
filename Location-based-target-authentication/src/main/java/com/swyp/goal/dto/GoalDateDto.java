package com.swyp.goal.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoalDateDto {

    @Schema(description = "달성 날짜", example = "2025-03-01")
    private LocalDate achievedAt;

    @Schema(description = "인증 여부", example = "true")
    private boolean achievedSuccess;
    
    
}