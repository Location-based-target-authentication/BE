package com.swyp.goal.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.swyp.point.enums.PointType;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swyp.goal.entity.DayOfWeek;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.entity.GoalAchievements;
import com.swyp.goal.entity.GoalAchievementsLog;
import com.swyp.goal.entity.GoalDay;
import com.swyp.goal.entity.GoalStatus;
import com.swyp.goal.repository.GoalAchievementsLogRepository;
import com.swyp.goal.repository.GoalAchievementsRepository;
import com.swyp.goal.repository.GoalDayRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.location.service.LocationService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalDayRepository goalDayRepository;
    private final GoalAchievementsRepository goalAchievementsRepository;
    private final GoalAchievementsLogRepository goalAchievementsLogRepository;
    private final LocationService  locationService;
    private final GoalPointHandler goalPointHandler;
    private final UserRepository userRepository;

    //전체 목표 조회
    public List<Goal> getGoalList(Long userId){
        return goalRepository.findByUserId(userId);
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

        if (ChronoUnit.DAYS.between(startDate, endDate) < 5) {
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
        //(포인트) 차감
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

    //(포인트) 생성한 목표 요일 조회
    public List<DayOfWeek> getSelectedDays(Long goalId) {
        List<GoalDay> goalDays = goalDayRepository.findByGoalId(goalId);
        return goalDays.stream()
                .map(GoalDay::getDayOfWeek)
                .collect(Collectors.toList());
    }
    // (포인트) 특정 날짜가 목표에 설정된 요일인지 확인
    public boolean checkIfSelectedDay(Goal goal, LocalDate date) {
        List<DayOfWeek> selectedDays = getSelectedDays(goal.getId());
        DayOfWeek today = DayOfWeek.fromJavaTime(date.getDayOfWeek());
        return selectedDays.contains(today);
    }

    //목표 달성 1차 인증 (goal의 위도 경도 확인 이후 100m이내에 있을시 achieved_count 를 +1함 )
    @Transactional
    public boolean validateGoalAchievement(Long userId, Long goalId, double latitude, double longitude){
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));
        //목표달성기록 테이블 로그에 이미 같은날의 인증성공 기록시 예외처리
        boolean alreadyAchievedTrue = goalAchievementsLogRepository.existsByUserIdAndGoalIdAndAchievedAtAndAchievedSuccess(userId, goalId, LocalDate.now(), true);
        //목표달성기록 테이블 로그에 이미 같은날의 인증 실패 기록 있을시 예외처리  
        boolean alreadyAchievedFalse = goalAchievementsLogRepository.existsByUserIdAndGoalIdAndAchievedAtAndAchievedSuccess(userId, goalId, LocalDate.now(), false);

        if(alreadyAchievedTrue){
            throw new IllegalStateException("오늘 이미 목표를 인증했습니다.");
        }
        // 위치 검증 
        Boolean validate =  locationService.verifyLocation(goalId, latitude, longitude);
        System.out.println(validate);
        if(validate){
            // 인증 기록 저장
            GoalAchievementsLog achievementsLog = new GoalAchievementsLog();
            achievementsLog.setUserId(userId);
            achievementsLog.setGoalId(goalId);
            achievementsLog.setAchievedSuccess(true);
            goalAchievementsLogRepository.save(achievementsLog);
            // 목표 달성 횟수 증가 
            goal.setAchievedCount(goal.getAchievedCount()+1);
            goalRepository.save(goal);
            // (포인트) 지급
            boolean isSelectedDay = checkIfSelectedDay(goal, LocalDate.now());
            AuthUser authUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            goalPointHandler.handleDailyAchievement(authUser, goal, isSelectedDay);
            return true;
        }
        else{
        	if(alreadyAchievedFalse) {
            	throw new IllegalStateException("DB상의 인설트 막힘 - 오늘 실패한 기록이 이미 존재합니다.(DB 중복 방지)");
            }
        	// 위치 검증 실패시 achieved_success = false와 함꼐 기록에 저장 ( 스케쥴러로 하루마다 achieved_success = false인것 삭제 해야됨 ) 
        	GoalAchievementsLog achievementsLog = new GoalAchievementsLog();
            achievementsLog.setUserId(userId);
            achievementsLog.setGoalId(goalId);
            achievementsLog.setAchievedSuccess(false);
            goalAchievementsLogRepository.save(achievementsLog);

            return false;
        }
        
    }
     //목표 달성시 목표 Status 'COMPLETE' 로 업데이트 후 목표 달성 기록 저장
     @Transactional
     public Goal updateGoalStatusToComplete(Long goalId, String socialId, boolean isSelectedDay){
         Goal goal = goalRepository.findById(goalId)
         .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));
 
         // Status가 ACTIVE인 애들만 완료 처리가능
         if (!goal.getStatus().equals(GoalStatus.ACTIVE)) {
             throw new IllegalArgumentException("활성화된 목표만 완료 처리할 수 있습니다.");
         }
 
         // 목표달성 횟수보다 실제 목표 달성횟수가 커야지 Complete 가능
         if (goal.getAchievedCount()<goal.getTargetCount()){
             throw new IllegalArgumentException("지정된 목표 달성 횟수를 채우지 못하셨습니다.");
         }
         AuthUser authUser = userRepository.findBySocialId(socialId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
         goalPointHandler.handleWeeklyGoalCompletion(authUser, goal);

         goal.setStatus(GoalStatus.COMPLETE);
         goalRepository.save(goal);
 
         GoalAchievements goalAchievements = new GoalAchievements();
         goalAchievements.setUserId(goal.getUserId());
         goalAchievements.setGoalId(goalId);
         goalAchievements.setName(goal.getName());
         goalAchievements.setTargetCount(goal.getTargetCount());
         goalAchievements.setAchievedCount(goal.getAchievedCount());
         goalAchievements.setStartDate(goal.getStartDate());
         goalAchievements.setEndDate(goal.getEndDate());
         // (포인트) 관련

         goalAchievementsRepository.save(goalAchievements);
         return goal;
     }

}
