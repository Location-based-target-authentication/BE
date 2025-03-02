package com.swyp.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompleteResponseDto {

	@Schema(description = "응답 메시지", example = "목표 달성 완료, 오류")
    private String message;
}
