package com.swyp.location.controller;

import com.swyp.global.dto.ErrorResponse;
import com.swyp.location.dto.LocationSearchResponse;
import com.swyp.location.exception.LocationNotFoundException;
import com.swyp.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@Tag(name = "위치", description = "위치 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LocationController {

    private final LocationService locationService;

    @Operation(
        summary = "위치 검색",
        description = "키워드로 위치를 검색합니다. (카카오 로컬 API 사용)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "검색 성공",
                content = @Content(schema = @Schema(implementation = LocationSearchResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "검색 결과 없음",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
        }
    )
    @GetMapping("/locations/search")
    public ResponseEntity<?> searchLocation(
            @Parameter(description = "검색 키워드", example = "스타벅스 강남") 
            @RequestParam String keyword) {
        try {
            return ResponseEntity.ok(locationService.searchLocation(keyword));
        } catch (LocationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @Operation(
        summary = "위치 검증",
        description = "현재 위치를 기준으로 100m 반경 내에 목표 장소가 있는지 확인합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "검증 성공",
                content = @Content(schema = @Schema(implementation = Boolean.class))
            )
        }
    )
    @GetMapping("/goals/{goalId}/location/verify")
    public ResponseEntity<Boolean> verifyLocation(
            @Parameter(description = "목표 ID", example = "1") 
            @PathVariable Long goalId,
            @Parameter(description = "현재 위도 (기준점)", example = "37.5665") 
            @RequestParam Double latitude,
            @Parameter(description = "현재 경도 (기준점)", example = "126.9780") 
            @RequestParam Double longitude) {
        return ResponseEntity.ok(locationService.verifyLocation(goalId, latitude, longitude));
    }
} 