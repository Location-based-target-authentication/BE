package com.swyp.point.service;
import com.swyp.goal.entity.Goal;
import java.time.DayOfWeek;
import com.swyp.goal.repository.GoalAchievementsLogRepository;
import com.swyp.goal.repository.GoalDayRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.point.enums.PointType;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
public class GoalPointHandler {

    private final PointService pointService;
    private final GoalRepository goalRepository;
    private final GoalDayRepository goalDayRepository;
    private final UserRepository userRepository;
    private final GoalAchievementsLogRepository goalAchievementsLogRepository;

    // 1. 목표 생성 시 포인트 차감
    @Transactional
    public void handleGoalCreation(Goal goal) {
        AuthUser authUser = userRepository.findById(goal.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없음"));

        boolean success = pointService.deductPoints(authUser, 500, PointType.GOAL_ACTIVATION, "목표 생성", goal.getId());
        if (!success) {
            throw new IllegalArgumentException("포인트 부족으로 목표 생성 불가");
        }
    }

    // 2. 목표 당일 달성 시 포인트 적립
    @Transactional
    public void handleDailyAchievement(AuthUser authUser, Goal goal, boolean isSelectedDay) {
        int points = 0;
        int dayCount = goalDayRepository.findByGoalId(goal.getId()).size();
        if (dayCount == 7) {
            points = 60;  // 7일 목표: 매일 60P
        } else {
            points = isSelectedDay ? 50 : 30; // 설정 요일: 50P, 비설정 요일: 30P
        }
        pointService.addPoints(authUser, points, PointType.ACHIEVEMENT, "당일 목표 달성", goal.getId());
        // 목표 달성 횟수 증가
        goalRepository.save(goal);
    }

    // 3. 목표 완료 시 보너스 지급
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleWeeklyGoalCompletion(AuthUser authUser, Goal goal) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        // 현재 주의 목표 달성 횟수 가져오기
        int weeklyAchievedCount = goalAchievementsLogRepository.countByGoalIdAndUserIdAndAchievedSuccessAndAchievedAtBetween(
                goal.getId(), authUser.getId(), true, startOfWeek, endOfWeek);

        // 6일 이하 달성 시 보너스 지급
        if (weeklyAchievedCount <= 6 && weeklyAchievedCount >= goal.getTargetCount()) {
            pointService.addPoints(authUser, 50, PointType.BONUS, "주간 목표 초과 달성 보너스", goal.getId());
            
        }

        // 7일 달성 시 보너스 지급 및 달성 여부 설정
        if (weeklyAchievedCount == 7) {
            pointService.addPoints(authUser, 60, PointType.BONUS, "7일 목표 완벽 달성 보너스", goal.getId());
            
        }

         // 6일이하, 7일 달성 보너시 지급 달성 여부 
    }

}

