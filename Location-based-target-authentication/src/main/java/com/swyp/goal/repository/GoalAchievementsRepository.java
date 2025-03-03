package com.swyp.goal.repository;

import java.util.List;

import com.swyp.social_login.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swyp.goal.entity.GoalAchievements;

@Repository
public interface GoalAchievementsRepository extends JpaRepository<GoalAchievements, Long> {

	List<GoalAchievements> findByUserId(Long userId);
	
	// goal_id를 NULL로 설정
    @Modifying
    @Query("UPDATE GoalAchievements g SET g.goal = NULL WHERE g.goal.id = :goalId")
    void updateGoalIdToNull(@Param("goalId") Long goalId);

	void deleteAllByUserId(Long userId);

}
