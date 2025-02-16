package com.swyp.location.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.function.Function;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

import com.swyp.location.dto.LocationSearchResponse;
import com.swyp.location.dto.KakaoApiResponse;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {
    
    @InjectMocks
    private LocationService locationService;
    
    @Mock
    private WebClient webClient;

    @Test
    @DisplayName("현재 위치 기준 100m 반경 내에 목표 장소가 있으면 true를 반환한다")
    void verifyLocation_WithinRadius_ReturnsTrue() {
        // given
        Long goalId = 1L;
        double currentLat = 37.623367;  // 현재 위치 (GS25 노원부에노점 근처)
        double currentLon = 127.084872;

        // when
        boolean result = locationService.verifyLocation(goalId, currentLat, currentLon);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("현재 위치 기준 100m 반경을 벗어나면 false를 반환한다")
    void verifyLocation_OutsideRadius_ReturnsFalse() {
        // given
        Long goalId = 1L;
        double currentLat = 37.566500;  // 현재 위치 (강남역)
        double currentLon = 126.978000;

        // when
        boolean result = locationService.verifyLocation(goalId, currentLat, currentLon);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("위치 검색 시 결과가 있으면 LocationSearchResponse를 반환한다")
    @SuppressWarnings("unchecked")
    void searchLocation_WithResults_ReturnsResponse() {
        // given
        String keyword = "GS25 노원부에노점";
        WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        KakaoApiResponse mockResponse = new KakaoApiResponse(List.of(
            new KakaoApiResponse.Document(
                "GS25 노원부에노점",
                "서울 노원구 공릉동 81",
                "서울 노원구 공릉로34길 62",
                "127.08487221991373",
                "37.623367069197776"
            )
        ));

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(any(Function.class));
        doReturn(headersSpec).when(headersSpec).header(anyString(), anyString());
        doReturn(headersSpec).when(headersSpec).accept(any());
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(Mono.just(mockResponse)).when(responseSpec).bodyToMono(KakaoApiResponse.class);

        // when
        LocationSearchResponse response = locationService.searchLocation(keyword);

        // then
        assertThat(response).isNotNull();
        assertThat(response.locations()).hasSize(1);
        assertThat(response.locations().get(0).placeName()).isEqualTo("GS25 노원부에노점");
    }
} 