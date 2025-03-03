package com.swyp.goal.repository;

import java.util.List;

import com.swyp.social_login.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swyp.goal.entity.Goal;
import com.swyp.goal.entity.GoalStatus;

// JpaRepository를 상속받으면 기본적인 CRUD 메서드를 자동으로 사용할 수 있습니다
// <Goal, Long>에서 Goal은 엔티티 타입, Long은 기본키의 타입입니다
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    

    //전체 조회 
    List<Goal> findByAuthUserId(Long authUserId);
    
    // 사용자 ID와 상태로 목표 조회
    List<Goal> findByAuthUserIdAndStatus(Long authUserId, GoalStatus status);
    
    List<Goal> findByStatus(GoalStatus status); // 상태로 목표 조회

    
    // 사용자 ID와 상태가 DRAFT 또는 ACTIVE인 목표 개수 조회

    long countByAuthUserIdAndStatusIn(Long authUserId, List<GoalStatus> statuses);

    // 목표에서 Status 상태에 따른 삭제 (현재 Complete인 애들 삭제를 위해 만듬 스케쥴링 제작)
    int deleteByStatus(GoalStatus Status);

    void deleteAllByAuthUserId(Long authUserId);

}
   

