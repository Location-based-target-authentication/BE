package com.swyp.Swagger.controller;

import com.swyp.Swagger.dto.location.LocationSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Tag(name = "위치", description = "위치 관련 API")
@RestController
@RequiredArgsConstructor
public class LocationSwaggerController {

    @SuppressWarnings("unused")
    private final WebClient webClient;
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    
    @Value("${kakao.local.search.url}")
    private String kakaoSearchUrl;

    @Operation(
        summary = "위치 검색",
        description = "키워드로 위치를 검색합니다. (카카오 로컬 API 사용)"
    )
    @GetMapping("/api/v1/locations/search")
    public LocationSearchResponse searchLocation(
        @Parameter(description = "검색 키워드", example = "스타벅스 강남") 
        @RequestParam String keyword
    ) {
        return null;  // 실제 구현은 service layer에서
    }
} 