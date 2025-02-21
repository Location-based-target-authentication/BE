package com.swyp.goal.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swyp.goal.entity.GoalStatus;
import com.swyp.goal.repository.GoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoalScheduledService {


    private final GoalRepository goalRepository;

    // 매주 일요일 (01:00:00)에 실행
    @Scheduled(cron = "0 0 1 * * SUN") 
    @Transactional
    public void deleetCompleteGoals(){
        int deletedCount = goalRepository.deleteByStatus(GoalStatus.COMPLETE);
        System.out.println("삭제된 목표 개수: " + deletedCount);
    }

}
