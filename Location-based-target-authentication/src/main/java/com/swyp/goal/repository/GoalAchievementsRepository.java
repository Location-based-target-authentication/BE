package com.swyp.goal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swyp.goal.entity.GoalAchievements;

@Repository
public interface GoalAchievementsRepository extends JpaRepository<GoalAchievements, Long> {

   

    

}
