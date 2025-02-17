package com.swyp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.entity.Goal;
import com.swyp.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalRestController.class)  // 컨트롤러 테스트용 설정
class GoalRestControllerTest {

    @Mock
    private GoalService goalService;

    @InjectMocks
    private GoalRestController goalRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(goalRestController).build();
    }

    @Autowired
    private ObjectMapper objectMapper;  // JSON 변환용

    
    
    
    @Test
    @DisplayName("목표 리스트 조회 API 테스트")
    void testGetGoalList() throws Exception {
        // given
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setName("운동하기");
        List<Goal> goalList = Collections.singletonList(goal);

        // when (목표 리스트 조회 시 Mock 응답 설정)
        Mockito.when(goalService.getGoalList(anyLong())).thenReturn(goalList);

        // then (API 호출 후 검증)
        mockMvc.perform(get("/api/v1/goals/check")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))  // 결과 배열 크기 확인
            .andExpect(jsonPath("$[0].name").value("운동하기"));  // 목표 이름 확인
    }

    @Test
    @DisplayName("목표 상세 조회 API 테스트")
    void testGetGoalDetail() throws Exception {
        // given
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setName("공부하기");

        // when (Mock 설정)
        Mockito.when(goalService.getGoalDetail(anyLong())).thenReturn(goal);

        // then (API 호출 및 검증)
        mockMvc.perform(post("/api/v1/goals/check/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.name").value("공부하기"));  // 목표 이름 확인
    }

    @Test
    @DisplayName("목표 생성 API 테스트")
    void testCreateGoal() throws Exception {
        // given
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setName("헬스");

        // when (Mock 설정)
        Mockito.when(goalService.createGoal(any(Goal.class), any(), any())).thenReturn(goal);

        // then (API 호출 및 검증)
        mockMvc.perform(post("/api/v1/goals/create")
                .param("status", "ACTIVE")
                .param("days", "MON", "WED", "FRI")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("헬스"));  // 목표 이름 확인
    }

    @Test
    @DisplayName("목표 삭제 API 테스트")
    void testDeleteGoal() throws Exception {
        // when (Mock 설정: 삭제할 때 예외 발생 안 하도록)
        Mockito.doNothing().when(goalService).deleteGoal(anyLong());

        // then (API 호출 및 검증)
        mockMvc.perform(delete("/api/v1/goals/1/delete"))
            .andExpect(status().isNoContent());
    }
}
