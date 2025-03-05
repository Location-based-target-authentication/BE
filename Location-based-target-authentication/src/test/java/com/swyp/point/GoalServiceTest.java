package com.swyp.point;

import com.swyp.goal.entity.Goal;
import com.swyp.goal.repository.GoalAchievementsLogRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.goal.service.GoalService;
import com.swyp.location.service.LocationService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
public class GoalServiceTest {

    @Autowired
    private GoalService goalService;

    @MockBean
    private GoalRepository goalRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LocationService locationService;

    @MockBean
    private GoalAchievementsLogRepository goalAchievementsLogRepository;

    @MockBean
    private PointRepository pointRepository;

    @MockBean
    private PointHistoryRepository pointHistoryRepository;

    @MockBean
    private GoalPointHandler goalPointHandler;

    @Test
    @Transactional
    public void testPointHistorySavedOnceOnGoalCompletion() {
        // Given
        Long userId = 1L;
        Long goalId = 100L;
        double latitude = 37.5665;
        double longitude = 126.9780;

        AuthUser mockUser = new AuthUser("1L", "testUser", "test@example.com", "123456789", SocialType.GOOGLE);
        Goal mockGoal = new Goal();
        mockGoal.setId(goalId);

        // Mock 데이터 설정
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        Mockito.when(goalRepository.findById(goalId)).thenReturn(Optional.of(mockGoal));
        Mockito.when(locationService.verifyLocation(goalId, latitude, longitude)).thenReturn(true);
        Mockito.when(goalAchievementsLogRepository.existsByUser_IdAndGoal_IdAndAchievedAtAndAchievedSuccess(userId, goalId, LocalDate.now(), true)).thenReturn(false);

        // 포인트 지급이 정상적으로 호출되도록 설정
        Mockito.doNothing().when(goalPointHandler).handleDailyAchievement(Mockito.any(AuthUser.class), Mockito.any(Goal.class), Mockito.anyBoolean());
        boolean result = goalService.validateGoalAchievement(userId, goalId, latitude, longitude);
        assertTrue(result); // 목표 인증 성공 확인

        // 포인트 히스토리 한 번만 저장
        Mockito.verify(pointHistoryRepository, Mockito.times(1)).save(Mockito.any(PointHistory.class));
    }
}

