package com.swyp.location.service;

import com.swyp.location.dto.LocationSearchResponse;
import com.swyp.location.dto.KakaoApiResponse;
import com.swyp.location.exception.LocationNotFoundException;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final WebClient webClient;
    private final GoalRepository goalRepository;
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    
    @Value("${kakao.local.search.url}")
    private String kakaoSearchUrl;

    public LocationSearchResponse searchLocation(String keyword) {
        try {
            KakaoApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword)
                        .queryParam("size", 15)
                        .queryParam("analyze_type", "similar")
                        .queryParam("sort", "accuracy")
                        .build())
                    .header("Authorization", "KakaoAK " + kakaoApiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(KakaoApiResponse.class)
                    .block();

            log.debug("Raw API Response: {}", response);

            if (response == null || response.documents().isEmpty()) {
                if (keyword.toLowerCase().contains("gs25") || 
                    keyword.toLowerCase().contains("cu") || 
                    keyword.toLowerCase().contains("seven")) {
                    response = webClient.get()
                            .uri(uriBuilder -> uriBuilder
                                .path("/v2/local/search/keyword.json")
                                .queryParam("query", keyword + "점")
                                .queryParam("size", 15)
                                .queryParam("analyze_type", "similar")
                                .queryParam("sort", "accuracy")
                                .build())
                            .header("Authorization", "KakaoAK " + kakaoApiKey)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(KakaoApiResponse.class)
                            .block();
                }
                
                if (response == null || response.documents().isEmpty()) {
                    log.warn("No search results found for keyword: {}", keyword);
                    throw new LocationNotFoundException("검색 결과가 없습니다. 다른 키워드로 검색해보세요.");
                }
            }

            return LocationSearchResponse.from(response);
        } catch (WebClientResponseException e) {
            log.error("Kakao API Error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while calling Kakao API", e);
            throw e;
        }
    }

    public Boolean verifyLocation(Long goalId, Double currentLatitude, Double currentLongitude) {
        // 실제 DB에서 목표 위치를 조회
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));
            
        double goalLatitude = goal.getLatitude().doubleValue();  // BigDecimal을 double로 변환
        double goalLongitude = goal.getLongitude().doubleValue();  // BigDecimal을 double로 변환
        int radiusMeters = goal.getRadius();  // DB에 저장된 반경

        log.info("현재 위치: ({}, {})", currentLatitude, currentLongitude);
        log.info("목표 위치: ({}, {})", goalLatitude, goalLongitude);
        log.info("허용 반경: {}m", radiusMeters);

        // 현재 위치를 중심으로 목표 위치가 반경 내에 있는지 확인
        boolean isWithin = isWithinRadius(
            currentLatitude, currentLongitude,  // 현재 위치 (중심)
            goalLatitude, goalLongitude,        // 목표 위치
            radiusMeters
        );

        log.info("위치 검증 결과: {}", isWithin);
        return isWithin;
    }

    private boolean isWithinRadius(
            double centerLat, double centerLon,  // 현재 위치 (중심)
            double targetLat, double targetLon,  // 목표 위치
            int radiusMeters) {
        
        final int R = 6371000; // 지구의 반지름 (미터)

        double latDistance = Math.toRadians(targetLat - centerLat);
        double lonDistance = Math.toRadians(targetLon - centerLon);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(centerLat)) * Math.cos(Math.toRadians(targetLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c;  // 미터 단위 거리

        return distance <= radiusMeters;
    }
} 