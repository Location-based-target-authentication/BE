package com.swyp.point;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.TestConfig.TestSecurityConfig;
import com.swyp.goal.controller.GoalRestController;
import com.swyp.goal.dto.GoalAchieveRequestDto;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.goal.service.GoalService;
import com.swyp.point.entity.Point;
import com.swyp.point.enums.PointType;
import com.swyp.point.repository.PointHistoryRepository;
import com.swyp.point.repository.PointRepository;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    @DisplayName("목표 인증 - 위치 검증 성공 시 응답 확인")
    public void testGoalAchievementResponse_PointsAwarded() throws Exception {
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

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(mockGoal));
        when(goalService.validateGoalAchievement(userId, goalId, latitude, longitude)).thenReturn(true);

        // 포인트 초기값
        AtomicInteger totalPoints = new AtomicInteger(2000);

        // `getTotalPoints()`가 최신 값 반환
        when(pointService.getTotalPoints(mockUser)).thenAnswer(invocation -> totalPoints.get());

        // 목표 인증 시 포인트 지급
        when(goalPointHandler.handleDailyAchievement(Mockito.any(AuthUser.class), Mockito.any(Goal.class), Mockito.anyBoolean()))
                .thenReturn(50);

        when(goalPointHandler.handleWeeklyGoalCompletion(Mockito.any(AuthUser.class), Mockito.any(Goal.class)))
                .thenReturn(50);

        mockMvc.perform(post("/api/v1/goals/{goalId}/achieve", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        // pointService.addPoints()가 호출되었는지 검증
        verify(pointService, times(1)).addPoints(mockUser, 50, PointType.ACHIEVEMENT, "당일 목표 달성", goalId);
        verify(pointService, times(1)).addPoints(mockUser, 50, PointType.BONUS, "주간 목표 초과 달성 보너스", goalId);
    }


//    @Test
//    @DisplayName("목표 인증 - 위치 검증 성공 시 응답 확인")
//    public void testGoalAchievementResponse_PointsAwarded() throws Exception {
//        Long userId = 1L;
//        Long goalId = 100L;
//        double latitude = 37.5665;
//        double longitude = 126.9780;
//
//        AuthUser mockUser = new AuthUser("1L", "testUser", "test@example.com", "123456789", SocialType.GOOGLE);
//        Goal mockGoal = new Goal();
//        mockGoal.setId(goalId);
//
//        GoalAchieveRequestDto requestDto = new GoalAchieveRequestDto();
//        requestDto.setUserId(userId);
//        requestDto.setLatitude(latitude);
//        requestDto.setLongitude(longitude);
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
//        when(goalRepository.findById(goalId)).thenReturn(Optional.of(mockGoal));
//        when(goalService.validateGoalAchievement(userId, goalId, latitude, longitude)).thenReturn(true);
//
//        // 포인트 초기값
//        AtomicInteger totalPoints = new AtomicInteger(2000);
//        // getTotalPoints()가 최신 값 반환하도록
//        when(pointService.getTotalPoints(mockUser)).thenAnswer(invocation -> totalPoints.get());
//        // 목표 인증 시 포인트 지급
//        when(goalPointHandler.handleDailyAchievement(Mockito.any(AuthUser.class), Mockito.any(Goal.class), Mockito.anyBoolean()))
//                .thenAnswer(invocation -> {
//                    AuthUser user = invocation.getArgument(0);
//                    Point userPoint = pointRepository.findByAuthUser(user).orElseGet(() -> {
//                        Point newPoint = new Point(user);
//                        newPoint.setTotalPoints(2000); // 초기 포인트 설정
//                        return newPoint;
//                    });
//
//                    int newPoints = userPoint.getTotalPoints() + 50;
//                    userPoint.setTotalPoints(newPoints);
//                    pointRepository.save(userPoint);
//                    return 50;
//                });
//
//        when(goalPointHandler.handleWeeklyGoalCompletion(Mockito.any(AuthUser.class), Mockito.any(Goal.class)))
//                .thenAnswer(invocation -> {
//                    AuthUser user = invocation.getArgument(0);
//                    Point userPoint = pointRepository.findByAuthUser(user).orElseGet(() -> {
//                        Point newPoint = new Point(user);
//                        newPoint.setTotalPoints(2000);
//                        return newPoint;
//                    });
//
//                    int newPoints = userPoint.getTotalPoints() + 50;
//                    userPoint.setTotalPoints(newPoints);
//                    pointRepository.save(userPoint);
//                    return 50;
//                });
//
//
//        mockMvc.perform(post("/api/v1/goals/{goalId}/achieve", goalId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(new ObjectMapper().writeValueAsString(requestDto)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.achievementStatus").value("성공"))
//                .andExpect(jsonPath("$.totalPoints").value(2100))  // totalPoints = 2000 + 50 + 50
//                .andExpect(jsonPath("$.currentPoints").value(50))  // 지급된 포인트 확인
//                .andExpect(jsonPath("$.bonusPoints").value(50))  // 보너스 포인트 확인
//                .andExpect(jsonPath("$.message").value("목표 인증에 성공했습니다."));
//    }

    @Test
    @DisplayName("목표 인증 - 위치 검증 실패 시 응답 확인")
    void testGoalAchievementFailure() throws Exception {
        // Given (Mock 설정)
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


        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(goalRepository.findById(goalId)).thenReturn(Optional.of(mockGoal));
        when(goalService.validateGoalAchievement(userId, goalId, latitude, longitude)).thenReturn(false);
        when(pointService.getTotalPoints(mockUser)).thenReturn(2000); // 기존 유저 총 포인트

        mockMvc.perform(post("/api/v1/goals/{goalId}/achieve", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achievementStatus").value("실패"))
                .andExpect(jsonPath("$.totalPoints").value(2000))
                .andExpect(jsonPath("$.currentPoints").value(0))
                .andExpect(jsonPath("$.bonusPoints").value(0))
                .andExpect(jsonPath("$.message").value("목표 위치가 현재 위치와 100m 이상 차이가 있거나, 오늘 이미 인증했습니다."));
    }
}
