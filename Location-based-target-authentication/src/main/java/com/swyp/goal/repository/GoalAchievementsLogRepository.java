package com.swyp.goal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swyp.goal.entity.GoalAchievementsLog;


@Repository
public interface GoalAchievementsLogRepository extends JpaRepository<GoalAchievementsLog, Long> {

     // 오늘 날짜 + 성공 기록(true)가 있는지 확인 있으면 true 없으면 false
     boolean existsByUserIdAndGoalIdAndAchievedAtAndAchievedSuccess(Long userId, Long goalId, LocalDate achievedAt, boolean achievedSuccess);

     // 주간 달성 횟수를 조회
     int countByGoalIdAndUserIdAndAchievedSuccessAndAchievedAtBetween(
             Long goalId, Long userId, boolean achievedSuccess, LocalDate startDate, LocalDate endDate);

     // goalId와 achievedSuccess가 true인 기록 조회
     List<GoalAchievementsLog> findByGoalIdAndAchievedSuccessIsTrue(Long goalId);
}
