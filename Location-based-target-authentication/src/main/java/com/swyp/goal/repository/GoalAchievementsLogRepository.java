package com.swyp.goal.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swyp.goal.entity.GoalAchievementsLog;


@Repository
public interface GoalAchievementsLogRepository extends JpaRepository<GoalAchievementsLog, Long> {

     // 오늘 날짜 + 성공 기록(true)가 있는지 확인 있으면 true 없으면 false
     boolean existsByUserIdAndGoalIdAndAchievedAtAndAchievedSuccess(Long userId, Long goalId, LocalDate achievedAt, boolean achievedSuccess);
     
     
}
