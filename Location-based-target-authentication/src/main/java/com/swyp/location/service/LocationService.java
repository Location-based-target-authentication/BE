package com.swyp.location.service;

import com.swyp.location.dto.LocationSearchResponse;
import com.swyp.location.dto.KakaoApiResponse;
import com.swyp.location.exception.LocationNotFoundException;
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
        // 1. 목표 위치 조회 (실제로는 DB에서 가져와야 함. 현재는 테스트데이터.)
        double goalLatitude = 37.623367069197776;  
        double goalLongitude = 127.08487221991373;
        int radiusMeters = 100;  

        log.info("목표 위치: ({}, {})", goalLatitude, goalLongitude);
        log.info("현재 위치: ({}, {})", currentLatitude, currentLongitude);
        log.info("허용 반경: {}m", radiusMeters);

        boolean isWithin = isWithinRadius(
            currentLatitude, currentLongitude,
            goalLatitude, goalLongitude,
            radiusMeters
        );

        log.info("위치 검증 결과: {}", isWithin);
        return isWithin;
    }

    private boolean isWithinRadius(
            double lat1, double lon1,
            double lat2, double lon2,
            int radiusMeters) {
        
        final int R = 6371000; // 지구의 반지름 (미터)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c;

        return distance <= radiusMeters;
    }
} 