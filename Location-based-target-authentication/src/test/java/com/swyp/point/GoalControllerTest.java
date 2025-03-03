package com.swyp.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.TestConfig.TestSecurityConfig;
import com.swyp.goal.dto.GoalAchieveRequestDto;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.goal.service.GoalService;
import com.swyp.point.entity.Point;
import com.swyp.point.entity.PointHistory;
import com.swyp.point.repository.PointHistoryRepository;
import com.swyp.point.repository.PointRepository;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class GoalControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private GoalService goalService;
    @MockBean
    private GoalPointHandler goalPointHandler;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private GoalRepository goalRepository;
    @MockBean
    private PointService pointService;
    @MockBean
    private PointRepository pointRepository;
    @MockBean
    private PointHistoryRepository pointHistoryRepository;
    @Test
    public void testGoalAchievementResponse_PointsAwarded() throws Exception {
        // Given (목표 및 사용자 설정)
        Long userId = 1L;
        Long goalId = 100L;
        double latitude = 37.5665;
        double longitude = 126.9780;

        AuthUser mockUser = new AuthUser("1L", "testUser", "test@example.com", "123456789", SocialType.GOOGLE);
        Goal mockGoal = new Goal();
        mockGoal.setId(goalId);

        GoalAchieveRequestDto requestDto = new GoalAchieveRequestDto();
        requestDto.setUserId(userId);
        requestDto.setLatitude(latitude);
        requestDto.setLongitude(longitude);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        Mockito.when(goalRepository.findById(goalId)).thenReturn(Optional.of(mockGoal));
        Mockito.when(goalService.validateGoalAchievement(userId, goalId, latitude, longitude)).thenReturn(true);
        Mockito.when(pointService.getUserPoints(mockUser)).thenReturn(2000); // 기존 포인트 설정

        // handleDailyAchievement()실행
        Mockito.doNothing().when(goalPointHandler).handleDailyAchievement(
                Mockito.any(AuthUser.class), Mockito.any(Goal.class), Mockito.anyBoolean()
        );

        // 목표 인증
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/goals/" + goalId + "/achieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());
        //포인트가 한번만 지급되는지
        Mockito.verify(goalPointHandler, Mockito.times(1))
                .handleDailyAchievement(Mockito.any(AuthUser.class), Mockito.any(Goal.class), Mockito.anyBoolean());
    }
}
