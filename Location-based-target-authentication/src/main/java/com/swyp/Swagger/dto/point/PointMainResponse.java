package com.swyp.Swagger.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 메인 페이지 응답")
public record PointMainResponse(
    @Schema(description = "사용자 ID", example = "1")
    Long userId,

    @Schema(description = "현재 포인트", example = "1000")
    Integer balance,

    @Schema(description = "이번 달 적립 포인트", example = "500")
    Integer monthlyEarned,

    @Schema(description = "이번 달 사용 포인트", example = "300")
    Integer monthlyUsed
) {} 