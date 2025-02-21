package com.swyp.point.service;

import com.swyp.goal.entity.DayOfWeek;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.point.enums.PointType;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalPointHandler {

    private final PointService pointService;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    // 1. 목표 생성 시 포인트 차감
    @Transactional
    public void handleGoalCreation(Goal goal) {
        // goal.getUserId()로 사용자 조회
        AuthUser authUser = userRepository.findById(goal.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없음"));
        // 포인트 차감 (목표 생성 시 500P 차감)
        boolean success = pointService.deductPoints(authUser, 500, PointType.GOAL_ACTIVATION, "목표 생성", goal.getId());
        if (!success) {
            throw new IllegalArgumentException("포인트 부족으로 목표 생성 불가");
        }
    }


    // 2. 목표 달성 시 포인트 적립
    @Transactional
    public void handleGoalCompletion(AuthUser authUser, Goal goal, List<DayOfWeek> selectedDays, boolean isSelectedDay) {

        // 포인트 적립 로직
        int points = 0;
        if (selectedDays.size() == 7) {
            points = 60;
        } else {
            points = isSelectedDay ? 50 : 30; // 설정 요일 달성 50p, 비설정 요일 달성 30p
        }

        // 포인트 지급
        pointService.addPoints(authUser, points, PointType.ACHIEVEMENT, "목표 달성", goal.getId());
        // 목표 수행 횟수 증가
        goal.setAchievedCount(goal.getAchievedCount() + 1);
        goalRepository.save(goal);
        // 주간 보너스 체크
        if (goal.getAchievedCount() >= goal.getTargetCount()) {
            pointService.addPoints(authUser, 50, PointType.BONUS, "주간 목표 초과 달성 보너스", goal.getId());
        }
        // 7일 목표 전부 달성 시 추가 보너스
        if (selectedDays.size() == 7 && goal.getAchievedCount() == goal.getTargetCount()) {
            pointService.addPoints(authUser, 60, PointType.BONUS, "7일 목표 완벽 달성 보너스", goal.getId());
        }
    }
}
