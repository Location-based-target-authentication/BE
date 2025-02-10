package com.swyp.Swagger.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "위치 검색 응답")
public record LocationSearchResponse(
    @Schema(description = "검색 결과 목록")
    List<LocationInfo> locations
) {} 