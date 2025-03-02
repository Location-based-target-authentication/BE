package com.swyp.point.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swyp.point.entity.PointHistory;
import com.swyp.social_login.entity.AuthUser;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByAuthUserId(Long userId);
    @Query("SELECT SUM(p.points) FROM PointHistory p WHERE p.authUser = :authUser" )
    Integer getTotalPointsByAuthUser(@Param("authUser") AuthUser authUser);
    
    
    // goalId를 NULL로 설정 ( 목표 DELETE 를 위한 쿼리 ) 
    @Modifying
    @Query("UPDATE PointHistory p SET p.goalId = NULL WHERE p.goalId = :goalId")
    void updateGoalIdToNull(@Param("goalId") Long goalId);
    
    @Modifying
    @Query("DELETE FROM PointHistory p WHERE p.authUser.id = :userId")
    void deleteByAuthUserId(@Param("userId") Long userId);
}
