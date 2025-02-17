package com.swyp.Swagger.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "위치 정보")
public record LocationInfo(
    @Schema(description = "장소명", example = "스타벅스 강남점")
    String placeName,

    @Schema(description = "주소", example = "서울 강남구 테헤란로 101")
    String address,

    @Schema(description = "도로명 주소", example = "서울 강남구 테헤란로 101")
    String roadAddress,

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude
) {} 