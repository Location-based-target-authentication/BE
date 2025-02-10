package com.swyp.Swagger.controller;

import com.swyp.Swagger.dto.location.LocationSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "위치", description = "위치 관련 API")
@RestController
public class LocationSwaggerController {

    @Operation(
        summary = "위치 검색",
        description = "위치를 검색합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "검색 성공",
                content = @Content(schema = @Schema(implementation = LocationSearchResponse.class))
            )
        }
    )
    @GetMapping("/api/v1/locations/search")
    public LocationSearchResponse searchLocation(
        @RequestParam String keyword
    ) {
        return null;
    }
} 