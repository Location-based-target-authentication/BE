package com.swyp.point.dto;


import com.swyp.goal.entity.Goal;
import com.swyp.point.enums.PointType;
import lombok.Getter;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointDedeductRequest {
    @Schema(description = "차감할 포인트 양", example = "1000")
    private int points;
    @Schema(description = "포인트 타입 (예: GIFT_STARBUCKS, GIFT_COUPON 등)", example = "GIFT_STARBUCKS")
    private PointType pointType;
    @Schema(description = "포인트 차감 설명", example = "스타벅스 기프티콘 지급")
    private String description;
    @Schema(description = "목표 ID", example = "null")
    private Long goalId;
}

