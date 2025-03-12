package com.swyp.point;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.swyp.goal.entity.DayOfWeek;
import com.swyp.goal.repository.GoalAchievementsLogRepository;
import com.swyp.goal.repository.GoalDayRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.point.entity.Point;
import com.swyp.point.enums.PointType;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.entity.GoalDay;


@ExtendWith(MockitoExtension.class)
class GoalPointHandlerTest {

    @Mock
    private PointService pointService;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private GoalDayRepository goalDayRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GoalAchievementsLogRepository goalAchievementsLogRepository;

    @InjectMocks
    private GoalPointHandler goalPointHandler;

    private AuthUser authUser;
    private Goal goal;

    @BeforeEach
    void setUp() {
        // 테스트용 AuthUser, Goal 객체
        authUser = new AuthUser();
        authUser.setId(1L);
        authUser.setUserId(100L);
        authUser.setUsername("testUser");

        goal = new Goal();
        // 목표 생성 시 사용자 id (AuthUser의 PK) 사용
        goal.setId(10L);
        goal.setAuthUserId(authUser.getId());
        // 목표 달성 대상 횟수 (예: 7일 목표면 7, 아니면 목표 요일 수)
        goal.setTargetCount(7);
        // 목표의 달성 횟수 초기값
        goal.setAchievedCount(0);
    }

    @Test
    @DisplayName("목표 생성 시 포인트 충분하면 200포인트 차감")
    void testHandleGoalCreation_success() {
        // pointService.getOrCreatePoint에서 충분한 포인트가 있는 Point 반환
        Point point = new Point(authUser);
        point.setTotalPoints(2000);
        when(userRepository.findById(goal.getAuthUserId())).thenReturn(Optional.of(authUser));
        when(pointService.getOrCreatePoint(authUser)).thenReturn(point);
        // 성공 시 deductPoints 호출
        when(pointService.deductPoints(eq(authUser), eq(200), eq(PointType.GOAL_ACTIVATION), anyString(), eq(goal.getId())))
                .thenReturn(true);
        //실행
        assertDoesNotThrow(() -> goalPointHandler.handleGoalCreation(goal));
        // 검증: 포인트 차감 호출 여부 확인
        verify(pointService, times(1)).deductPoints(eq(authUser), eq(200), eq(PointType.GOAL_ACTIVATION), anyString(), eq(goal.getId()));
    }

    @Test
    @DisplayName("목표 생성 시 포인트 부족하면 예외 발생")
    void testHandleGoalCreation_insufficientPoints() {
        // pointService.getOrCreatePoint에서 포인트가 부족한 경우
        Point point = new Point(authUser);
        point.setTotalPoints(100);  // 200포인트 미만
        when(userRepository.findById(goal.getAuthUserId())).thenReturn(Optional.of(authUser));
        when(pointService.getOrCreatePoint(authUser)).thenReturn(point);

        // 실행 시 예외 발생 확인
        Exception exception = assertThrows(IllegalArgumentException.class, () -> goalPointHandler.handleGoalCreation(goal));
        assertTrue(exception.getMessage().contains("포인트 부족"));
    }
    private GoalDay createGoalDay(Long goalId, DayOfWeek dayOfWeek) {
        GoalDay goalDay = new GoalDay();
        goalDay.setGoalId(goalId);
        goalDay.setDayOfWeek(dayOfWeek);
        return goalDay;
    }

    @Test
    @DisplayName("7일 목표: 일일 달성 시 60포인트 지급")
    void testHandleDailyAchievement_for7DayGoal() {
        Long goalId = goal.getId(); // goal에서 ID 가져오기

        when(goalDayRepository.findByGoalId(goalId)).thenReturn(Arrays.asList(
                createGoalDay(goalId, DayOfWeek.MON),
                createGoalDay(goalId, DayOfWeek.TUE),
                createGoalDay(goalId, DayOfWeek.WED),
                createGoalDay(goalId, DayOfWeek.THU),
                createGoalDay(goalId, DayOfWeek.FRI),
                createGoalDay(goalId, DayOfWeek.SAT),
                createGoalDay(goalId, DayOfWeek.SUN)
        ));

        // pointService.addPoints는 예외 없이
        doNothing().when(pointService).addPoints(eq(authUser), eq(60), eq(PointType.ACHIEVEMENT), anyString(), eq(goal.getId()));
        int awardedPoints = goalPointHandler.handleDailyAchievement(authUser, goal, true);
        assertEquals(60, awardedPoints);
    }

    @Test
    @DisplayName("6일 이하 목표: 설정한 요일(선택 요일) 달성 시 50포인트 지급")
    void testHandleDailyAchievement_forSelectedDay() {
        Long goalId = goal.getId();
        // 목표에 연결된 day 수가 3개라고 가정 (6일 이하 목표) -- 월수금
        when(goalDayRepository.findByGoalId(goalId)).thenReturn(Arrays.asList(
                createGoalDay(goalId, DayOfWeek.MON),
                createGoalDay(goalId, DayOfWeek.WED),
                createGoalDay(goalId, DayOfWeek.FRI)
        ));
        // 선택한 요일일 경우 50포인트 지급
        doNothing().when(pointService).addPoints(eq(authUser), eq(50), eq(PointType.ACHIEVEMENT), anyString(), eq(goal.getId()));

        int awardedPoints = goalPointHandler.handleDailyAchievement(authUser, goal, true);
        assertEquals(50, awardedPoints);
    }

    @Test
    @DisplayName("6일 이하 목표: 선택하지 않은 요일 달성 시 30포인트 지급")
    void testHandleDailyAchievement_forNonSelectedDay() {
        Long goalId = goal.getId();
        // 목표에 연결된 day 수가 3개라고 가정 (6일 이하 목표) -- 월수금
        when(goalDayRepository.findByGoalId(goalId)).thenReturn(Arrays.asList(
                createGoalDay(goalId, DayOfWeek.MON),
                createGoalDay(goalId, DayOfWeek.WED),
                createGoalDay(goalId, DayOfWeek.FRI)
        ));
        // 선택하지 않은 요일이면 30포인트 지급
        doNothing().when(pointService).addPoints(eq(authUser), eq(30), eq(PointType.ACHIEVEMENT), anyString(), eq(goal.getId()));

        int awardedPoints = goalPointHandler.handleDailyAchievement(authUser, goal, false);
        assertEquals(30, awardedPoints);
    }

    @Test
    @DisplayName("주간 목표 보너스: 7일 목표 완벽 달성 시 60포인트 지급")
    void testHandleWeeklyGoalCompletion_for7DayComplete() {
        // 오늘 날짜를 기준으로 주의 시작/끝을 결정하므로, 테스트 시 현재 요일을 주간 목표의 마지막 요일로 세팅
        Long goalId = goal.getId();
        LocalDate today = LocalDate.now();
        DayOfWeek currentMyDay = DayOfWeek.fromJavaTime(today.getDayOfWeek());

        // 목표 요일 목록에 현재 요일이 포함되도록 설정
        when(goalDayRepository.findByGoalId(goalId)).thenReturn(Arrays.asList(
                createGoalDay(goalId, currentMyDay)
        ));
        // weeklyAchievedCount가 7이면 7일 목표 완벽 달성으로 처리
        when(goalAchievementsLogRepository.countByGoal_IdAndUser_IdAndAchievedSuccessAndAchievedAtBetween(
                eq(goalId), eq(authUser.getId()), anyBoolean(), any(), any()))
                .thenReturn(7);
        doNothing().when(pointService).addPoints(eq(authUser), eq(60), eq(PointType.BONUS), anyString(), eq(goal.getId()));

        int bonus = goalPointHandler.handleWeeklyGoalCompletion(authUser, goal);
        assertEquals(60, bonus);
    }



    @Test
    @DisplayName("주간 목표 보너스: 6일 이하 목표 달성 시 목표 달성 횟수가 targetCount 이상이면 50포인트 지급")
    void testHandleWeeklyGoalCompletion_for6DayBonus() {
        // 6일 이하 목표의 경우 targetCount를 3개라고 가정 (예: 설정 요일 3개)
        goal.setTargetCount(3);
        // 목표 요일 목록에 현재 요일이 마지막 요일로 설정되도록 함
        LocalDate today = LocalDate.now();
        DayOfWeek currentMyDay = DayOfWeek.fromJavaTime(today.getDayOfWeek());
        when(goalDayRepository.findByGoalId(goal.getId())).thenReturn(Arrays.asList(
                createGoalDay(goal.getId(), currentMyDay) // 수정된 부분
        ));

        // weeklyAchievedCount가 targetCount 이상(예: 3)이면서 6일 이하 목표인 경우
        when(goalAchievementsLogRepository.countByGoal_IdAndUser_IdAndAchievedSuccessAndAchievedAtBetween(
                eq(goal.getId()), eq(authUser.getId()), anyBoolean(), any(), any()))
                .thenReturn(3);
        doNothing().when(pointService).addPoints(eq(authUser), eq(50), eq(PointType.BONUS), anyString(), eq(goal.getId()));

        int bonus = goalPointHandler.handleWeeklyGoalCompletion(authUser, goal);
        assertEquals(50, bonus);
    }

    @Test
    @DisplayName("주간 목표 보너스: 마지막 요일이 아닐 경우 IllegalStateException 발생")
    void testHandleWeeklyGoalCompletion_notLastDay() {
        // 목표 요일 목록에 현재 요일보다 큰 값(내림차순 정렬 시 첫번째가 아님)을 세팅
        LocalDate today = LocalDate.now();
        DayOfWeek currentMyDay = DayOfWeek.fromJavaTime(today.getDayOfWeek());
        // 예를 들어, 목표 요일 목록이 현재 요일보다 큰 값이 없으면 마지막 요일 조건 미충족
        when(goalDayRepository.findByGoalId(goal.getId())).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                goalPointHandler.handleWeeklyGoalCompletion(authUser, goal)
        );
        assertTrue(exception.getMessage().contains("설정된 목표 요일이 없음"));
    }
}
