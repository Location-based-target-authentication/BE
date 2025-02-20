package com.swyp.goal.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.swyp.point.enums.PointType;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swyp.goal.entity.DayOfWeek;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.entity.GoalDay;
import com.swyp.goal.entity.GoalStatus;
import com.swyp.goal.repository.GoalDayRepository;
import com.swyp.goal.repository.GoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalDayRepository goalDayRepository;
    private final GoalPointHandler goalPointHandler;

    //전체 목표 조회
    public List<Goal> getGoalList(Long uesrId){
        return goalRepository.findByUserId(uesrId);
    }

    //목표 상세 조회
    public Goal getGoalDetail(Long goalId) {
          return goalRepository.findById(goalId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));
    }

    // 임시저장된 목표만 조회
    public List<Goal> getDraftGoalList(Long userId){
        return goalRepository.findByUserIdAndStatus(userId, GoalStatus.DRAFT);
    }
    
    //목표 생성
    @Transactional
    public Goal createGoal(Goal goal, String statusCheck, List<DayOfWeek> selectedDays) {
        LocalDate startDate = goal.getStartDate();
        LocalDate endDate = goal.getEndDate();
        String name = goal.getName();

        // 목표 개수 조회 (DRAFT 또는 ACTIVE 상태인 목표만)
        List<GoalStatus> statuses = List.of(GoalStatus.DRAFT, GoalStatus.ACTIVE);
        long count = goalRepository.countByUserIdAndStatusIn(goal.getUserId(), statuses);
        
        if (count >= 3) {
            throw new IllegalArgumentException("목표는 최대 3개까지만 생성할 수 있습니다.");
        }

        // 목표 이름 유효성 검사
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("목표 이름은 필수 입력 사항입니다.");
        }

        if (name.length() > 20 || name.length() < 2) {
            throw new IllegalArgumentException("목표 이름은 2~20자 이내여야 합니다.");
        }

        // 시작일과 종료일 유효성 검사
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("시작일은 오늘 이후여야 합니다.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }

        if (ChronoUnit.DAYS.between(startDate, endDate) < 7) {
            throw new IllegalArgumentException("종료일은 시작일 기준으로 최소 1주일 뒤여야 합니다.");
        }

        if (ChronoUnit.DAYS.between(startDate, endDate) > 90) {
            throw new IllegalArgumentException("종료일은 시작일 기준으로 최대 3개월 이내여야 합니다.");
        }

        if(statusCheck.equals("DRAFT")){
            goal.setStatus(GoalStatus.DRAFT);
        }else if(statusCheck.equals("ACTIVE")){
            goal.setStatus(GoalStatus.ACTIVE);
        }else{
            goal.setStatus(GoalStatus.DRAFT);
        }

        // 목표 수행 횟수 계산
        int targetCount = calculateTargetCount(startDate, endDate, selectedDays);
        goal.setTargetCount(targetCount); // 목표 수행 횟수 설정

        // 목표 저장
        Goal savedGoal = goalRepository.save(goal);
        goalPointHandler.handleGoalCreation(savedGoal);

        // 선택된 요일 저장
        for (DayOfWeek day : selectedDays) {
            GoalDay goalDay = new GoalDay();
            goalDay.setGoalId(savedGoal.getId());
            goalDay.setDayOfWeek(day);
            goalDayRepository.save(goalDay);
        }

        return savedGoal;
    }

    // 목표 수행 횟수 계산 메서드
    private int calculateTargetCount(LocalDate startDate, LocalDate endDate, List<DayOfWeek> selectedDays) {
        Set<DayOfWeek> daysSet = new HashSet<>(selectedDays); // 선택된 요일을 Set으로 변환
        int count = 0;

        // 시작일부터 종료일까지 반복
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // java.time.DayOfWeek를 사용자 정의 DayOfWeek로 변환
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(date.getDayOfWeek().name().substring(0, 3).toUpperCase());
            
            if (daysSet.contains(dayOfWeek)) {
                count++; // 선택된 요일이면 카운트 증가
            }
        }

        return count; // 총 수행 횟수 반환
    }

    //목표 상태 업데이트 ,프론트에서 status를 받아서 업데이트
    @Transactional
    public Goal updateGoalStatus(Long goalId, String status) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));
        
        // 문자열을 GoalStatus로 변환
        GoalStatus goalStatus = GoalStatus.valueOf(status.toUpperCase());
        goal.setStatus(goalStatus);
        goalRepository.save(goal);
        return goal;
        
    }

    //목표 수정
    @Transactional
    public Goal updateGoal(Long goalId, Goal newgoal){
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));
        
        if(!goal.getStatus().equals(GoalStatus.DRAFT)){
            throw new IllegalArgumentException("임시저장 목표만 수정할 수 있습니다.");
        }
        
        goal.setName(newgoal.getName());
        goal.setStartDate(newgoal.getStartDate());
        goal.setEndDate(newgoal.getEndDate());
        goal.setLocationName(newgoal.getLocationName());
        goal.setLatitude(newgoal.getLatitude());
        goal.setLongitude(newgoal.getLongitude());
        goal.setRadius(newgoal.getRadius());

        return goalRepository.save(goal);
    }

    //목표 삭제
    @Transactional
    public void deleteGoal(Long goalId){
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));
        
         // 상태가 DRAFT 또는 ACTIVE인지 확인
    if (!goal.getStatus().equals(GoalStatus.DRAFT) && !goal.getStatus().equals(GoalStatus.ACTIVE)) {
        throw new IllegalArgumentException("임시저장 또는 활성화 목표만 삭제할 수 있습니다.");
    }
        goalRepository.deleteById(goalId);
    }
}
