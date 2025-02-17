package com.swyp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swyp.entity.Goal;
import com.swyp.entity.GoalStatus;

// JpaRepository를 상속받으면 기본적인 CRUD 메서드를 자동으로 사용할 수 있습니다
// <Goal, Long>에서 Goal은 엔티티 타입, Long은 기본키의 타입입니다
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    

    //전체 조회 
    List<Goal> findByUserId(Long userId);
    
    // 사용자 ID와 상태로 목표 조회
    List<Goal> findByUserIdAndStatus(Long userId, GoalStatus status);
    
    // 사용자 ID와 상태가 DRAFT 또는 ACTIVE인 목표 개수 조회

    long countByUserIdAndStatusIn(Long userId, List<GoalStatus> statuses);

}
   

