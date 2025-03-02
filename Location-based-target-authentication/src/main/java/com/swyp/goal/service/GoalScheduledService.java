package com.swyp.goal.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swyp.goal.entity.Goal;
import com.swyp.goal.entity.GoalStatus;
import com.swyp.goal.repository.GoalAchievementsLogRepository;
import com.swyp.goal.repository.GoalAchievementsRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.point.repository.PointHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoalScheduledService {

    private final GoalRepository goalRepository;
    private final GoalAchievementsRepository goalAchievementsRepository;
    private final GoalAchievementsLogRepository goalAchievementsLogRepository;
    private final PointHistoryRepository pointHistoryRepository;

    // 매주 일요일 (01:00:00)에 실행, Goal테이블의 Status가 COMPLETE인 것 삭제 (DB 저장공간 관리)
    @Scheduled(cron = "0 0 1 * * SUN")
    @Transactional
    public void deleteCompleteGoals() {
        // 상태가 COMPLETE인 목표 조회
        List<Goal> completeGoals = goalRepository.findByStatus(GoalStatus.COMPLETE);

        // 각 목표에 대해 처리
        for (Goal goal : completeGoals) {
            Long goalId = goal.getId();

            // goal_achievements 테이블의 goal_id를 NULL로 설정
            goalAchievementsRepository.updateGoalIdToNull(goalId);

            // points_history 테이블의 goal_id를 NULL로 설정
            pointHistoryRepository.updateGoalIdToNull(goalId);

            // 목표 삭제
            goalRepository.deleteById(goalId);
        }

        System.out.println("삭제된 목표 개수: " + completeGoals.size());
    }
}