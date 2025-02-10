package com.swyp.Swagger.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기프트 구매 요청")
public record GiftPurchaseRequest(
    @Schema(description = "차감할 포인트", example = "100")
    Integer points,

    @Schema(description = "기프트 ID", example = "1")
    Long giftId
) {} 