package com.swyp.goal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swyp.goal.entity.DayOfWeek;
import com.swyp.goal.entity.GoalDay;

@Repository
public interface GoalDayRepository extends JpaRepository<GoalDay, Long> {
    
    // 특정 목표 ID에 대한 반복 요일 목록 조회
    List<GoalDay> findByGoalId(Long goalId);
}