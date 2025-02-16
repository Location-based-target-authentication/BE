package com.swyp.location.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.swyp.location.service.LocationService;
import com.swyp.location.dto.LocationSearchResponse;
import com.swyp.location.dto.LocationInfo;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @Test
    @DisplayName("위치 검증 API 호출 시 검증 결과를 반환한다")
    void verifyLocation() throws Exception {
        // given
        Long goalId = 1L;
        double latitude = 37.623367;
        double longitude = 127.084872;

        when(locationService.verifyLocation(goalId, latitude, longitude))
            .thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/v1/goals/{goalId}/location/verify", goalId)
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude)))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("위치 검색 API 호출 시 검색 결과를 반환한다")
    void searchLocation() throws Exception {
        // given
        String keyword = "GS25 노원부에노점";
        LocationSearchResponse mockResponse = new LocationSearchResponse(List.of(
            new LocationInfo(
                "GS25 노원부에노점",
                "서울 노원구 공릉동 81",
                "서울 노원구 공릉로34길 62",
                37.623367069197776,
                127.08487221991373
            )
        ));

        when(locationService.searchLocation(keyword))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/locations/search")
                .param("keyword", keyword))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.locations[0].placeName").value("GS25 노원부에노점"));
    }
} 