package com.swyp.point;

import com.swyp.goal.entity.Goal;
import com.swyp.goal.entity.GoalStatus;
import com.swyp.point.entity.Point;
import com.swyp.point.entity.PointHistory;
import com.swyp.point.enums.PointType;
import com.swyp.point.repository.PointHistoryRepository;
import com.swyp.point.repository.PointRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class PointControllerTest {
    @Autowired
    private PointService pointService;
    @MockBean
    private PointRepository pointRepository;
    @MockBean
    private PointHistoryRepository pointHistoryRepository;
    @MockBean
    private UserRepository userRepository;

    @Test
    public void testAddPoints() {
        AuthUser mockUser = new AuthUser("1L", "mj", "jj@naver.com", "123123123", SocialType.GOOGLE);
        Goal mockGoal = new Goal();
        mockGoal.setId(10L);
        mockGoal.setUserId(1L);
        mockGoal.setName("매일 아침 조깅");
        mockGoal.setStatus(GoalStatus.ACTIVE);
        mockGoal.setStartDate(LocalDate.of(2025, 3, 1));
        mockGoal.setEndDate(LocalDate.of(2025, 6, 1));
        mockGoal.setLocationName("한강공원");
        mockGoal.setLatitude(new BigDecimal("37.5665"));
        mockGoal.setLongitude(new BigDecimal("126.9780"));
        mockGoal.setRadius(100);
        mockGoal.setTargetCount(10);
        mockGoal.setAchievedCount(3);
        mockGoal.setCreatedAt(LocalDateTime.of(2025, 2, 22, 10, 15, 30));
        mockGoal.setUpdatedAt(LocalDateTime.of(2025, 2, 22, 12, 0, 0));

        Mockito.when(userRepository.findByUserId(1L)).thenReturn(Optional.of(mockUser));
        Point mockPoint = new Point(mockUser);
        mockPoint.setTotalPoints(2000); // 기존 포인트를 2000으로 설정

        Mockito.when(pointRepository.findByAuthUser(mockUser)).thenReturn(Optional.of(mockPoint));

        //목표 인증
        pointService.addPoints(mockUser, 50, PointType.ACHIEVEMENT, "테스트 포인트 지급", 10L);

        // Then
        assertEquals(2050, mockPoint.getTotalPoints()); // 포인트가 증가했는지 확인
        Mockito.verify(pointRepository, Mockito.times(1)).save(Mockito.any(Point.class)); // 한 번만 저장 확인
        Mockito.verify(pointHistoryRepository, Mockito.times(1)).save(Mockito.any(PointHistory.class)); // 이력 저장 확인
    }
}
