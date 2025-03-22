package com.swyp.goal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tags.shaded.org.apache.xpath.operations.Minus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swyp.goal.dto.GoalCreateRequest;
import com.swyp.goal.dto.GoalUpdateDto;
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
import com.swyp.point.repository.PointHistoryRepository;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import com.swyp.users.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    private final PointHistoryRepository pointHistoryRepository;



    //전체 목표 조회
    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    //전체 목표 조회 (UserId로 조회)
    public List<Goal> getGoalList(Long id){
        return goalRepository.findByAuthUserId(id);
    }

    //목표 상세 조회 (GoalId로 조회 )
    public Goal getGoalDetail(Long goalId) {
        return goalRepository.findById(goalId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));
    }

    //완료 목표 전체 조회(UserId로 조회)
    public List<GoalAchievements> getGoalAchievementsList(Long id){
        return goalAchievementsRepository.findByUser_Id(id);
    }

    // 임시저장된 목표만 조회 ( 사용 x )
    public List<Goal> getDraftGoalList(Long id){
        return goalRepository.findByAuthUserIdAndStatus(id, GoalStatus.DRAFT);
    }

    //목표 생성
    @Transactional
    public Goal createGoal(GoalCreateRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        String name = request.getName();

        // 목표 개수 제한 검증
        List<GoalStatus> statuses = List.of(GoalStatus.DRAFT, GoalStatus.ACTIVE);
        long count = goalRepository.countByAuthUserIdAndStatusIn(request.getUserId(), statuses);
        // if (count >= 3) {
        //     throw new IllegalArgumentException("목표는 최대 3개까지만 생성할 수 있습니다.");
        // }

        // // 목표 이름 유효성 검사
        // if (name == null || name.isEmpty()) {
        //     throw new IllegalArgumentException("목표 이름은 필수 입력 사항입니다.");
        // }
        // if (name.length() > 20 || name.length() < 2) {
        //     throw new IllegalArgumentException("목표 이름은 2~20자 이내여야 합니다.");
        // }

        // // 시작일과 종료일 유효성 검사
        // if (startDate.isBefore(LocalDate.now())) {
        //     throw new IllegalArgumentException("시작일은 오늘 이후여야 합니다.");
        // }
        // if (endDate.isBefore(startDate)) {
        //     throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        // }
        // if (ChronoUnit.DAYS.between(startDate, endDate) < 7) {
        //     throw new IllegalArgumentException("종료일은 시작일 기준으로 최소 1주일 뒤여야 합니다.");
        // }
        // if (ChronoUnit.DAYS.between(startDate, endDate) > 90) {
        //     throw new IllegalArgumentException("종료일은 시작일 기준으로 최대 3개월 이내여야 합니다.");
        // }

        // Goal 객체 생성 후 데이터 설정
        Goal goal = new Goal();
        goal.setUserId(request.getUserId());  // user_id 설정
        goal.setAuthUserId(request.getUserId()); // auth_user_id 설정
        goal.setName(request.getName());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setLocationName(request.getLocationName());
        goal.setLatitude(request.getLatitude());
        goal.setLongitude(request.getLongitude());
        goal.setStatus(request.getStatus().equals("ACTIVE") ? GoalStatus.ACTIVE : GoalStatus.DRAFT);

        // 목표 수행 횟수 계산 및 설정
        int targetCount = calculateTargetCount(startDate, endDate, request.getSelectedDays());
        goal.setTargetCount(targetCount);

        // 목표 저장
        Goal savedGoal = goalRepository.save(goal);

        // (포인트) 차감
        goalPointHandler.handleGoalCreation(savedGoal);

        // 선택된 요일 저장
        for (DayOfWeek day : request.getSelectedDays()) {
            GoalDay goalDay = new GoalDay();
            goalDay.setGoalId(savedGoal.getId());
            goalDay.setDayOfWeek(day);
            goalDayRepository.save(goalDay);
        }

        return savedGoal;
    }


    //목표 상태 업데이트 ,프론트에서 status를 받아서 업데이트
    @Transactional
    public Goal updateGoalStatus(Long goalId, String status) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        try {
            GoalStatus goalStatus = GoalStatus.valueOf(status.toUpperCase()); // 🔥 예외 발생 가능
            goal.setStatus(goalStatus);
            goal.setUpdatedAt(LocalDateTime.now());
            goalRepository.save(goal);
            return goal;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 상태 값입니다: " + status);
        }
    }

    //목표 수정
    @Transactional
    public Goal updateGoal(Long goalId, GoalUpdateDto dto) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        if (!goal.getStatus().equals(GoalStatus.DRAFT)) {
            throw new IllegalArgumentException("임시 저장된 목표만 수정할 수 있습니다.");
        }

        if (dto.getName() != null) goal.setName(dto.getName());
        if (dto.getStartDate() != null) goal.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) goal.setEndDate(dto.getEndDate());
        if (dto.getLocationName() != null) goal.setLocationName(dto.getLocationName());
        if (dto.getLatitude() != null) goal.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) goal.setLongitude(dto.getLongitude());
        if (dto.getRadius() != null) goal.setRadius(dto.getRadius());


        goal.setUpdatedAt(LocalDateTime.now());

        return goalRepository.save(goal);
    }

    //목표 삭제
    @Transactional
    public void deleteGoal(Long goalId) {
        // 목표 조회
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        // 상태가 DRAFT 또는 ACTIVE인지 확인
        if (!goal.getStatus().equals(GoalStatus.DRAFT) && !goal.getStatus().equals(GoalStatus.ACTIVE)) {
            throw new IllegalArgumentException("임시저장 또는 활성화 목표만 삭제할 수 있습니다.");
        }

        // goal_achievements 테이블의 goal_id를 NULL로 설정, points_history 테이블의 goal_id를 NULL로 설정.
        goalAchievementsRepository.updateGoalIdToNull(goalId);
        pointHistoryRepository.updateGoalIdToNull(goalId);


        // 목표 삭제
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
    public boolean validateGoalAchievement(Long id, Long goalId, double latitude, double longitude){
        try {
            Goal goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

            // 목표 상태 검증 추가
            if (!goal.getStatus().equals(GoalStatus.ACTIVE)) {
                throw new IllegalStateException("활성화된 목표만 인증할 수 있습니다.");
            }

            LocalDate today = LocalDate.now();

            //목표달성기록 테이블 로그에 이미 같은날의 인증성공 기록시 예외처리
            boolean alreadyAchievedTrue = goalAchievementsLogRepository.existsByUser_IdAndGoal_IdAndAchievedAtAndAchievedSuccess(id, goalId, today, true);
            //목표달성기록 테이블 로그에 이미 같은날의 인증 실패 기록 있을시 예외처리
            boolean alreadyAchievedFalse = goalAchievementsLogRepository.existsByUser_IdAndGoal_IdAndAchievedAtAndAchievedSuccess(id, goalId, today, false);

            if(alreadyAchievedTrue){
                throw new IllegalStateException("오늘 이미 목표를 인증했습니다.");
            }

            // 위치 검증
            Boolean validate = locationService.verifyLocation(goalId, latitude, longitude);
            log.info("위치 검증 결과: {}", validate);

            if(validate){
                // 인증 기록 저장
                GoalAchievementsLog achievementsLog = new GoalAchievementsLog();
                AuthUser authUser = userRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + id));
                achievementsLog.setUser(authUser);
                achievementsLog.setGoal(goal);
                achievementsLog.setAchievedSuccess(true);
                goalAchievementsLogRepository.save(achievementsLog);

                // 목표 달성 횟수 증가
                goal.setAchievedCount(goal.getAchievedCount()+1);
                goal.setUpdatedAt(LocalDateTime.now());

                // 목표 달성 횟수가 목표 횟수에 도달하면 COMPLETE로 변경
                if (goal.getAchievedCount() >= goal.getTargetCount()) {
                    goal.setStatus(GoalStatus.COMPLETE);
                }
                goalRepository.save(goal);

                // (포인트) 지급
                boolean isSelectedDay = checkIfSelectedDay(goal, today);
                goalPointHandler.handleDailyAchievement(authUser, goal, isSelectedDay);
                // (포인트) 보너스 지급
                goalPointHandler.handleWeeklyGoalCompletion(authUser, goal);
                return true;
            } else {
                if(alreadyAchievedFalse) {
                	return false;
                }
                // 위치 검증 실패시 achieved_success = false와 함께 기록 저장
                GoalAchievementsLog achievementsLog = new GoalAchievementsLog();
                AuthUser authUser = userRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                achievementsLog.setUser(authUser);
                achievementsLog.setGoal(goal);
                achievementsLog.setAchievedSuccess(false);
                goalAchievementsLogRepository.save(achievementsLog);

                return false;
            }
        } catch (Exception e) {
            log.error("목표 인증 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }
     //목표 달성시 목표 Status 'COMPLETE' 로 업데이트 후 목표 달성 기록 저장
     @Transactional
     public Goal updateGoalStatusToComplete(Long goalId, Long id){
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

         // (포인트) 해당 목표를 통해 얻은 포인트 총합 계산 (ACHIEVEMENT & BONUS 타입만)
         AuthUser authUser = userRepository.findById(id)
                 .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
         Integer totalEarnedPoints = pointHistoryRepository.getTotalPointsByAuthUser(authUser);
         totalEarnedPoints = (totalEarnedPoints != null) ? totalEarnedPoints : 0;


        //GoalAchievements 테이블로 day를 넘기기 위한 로직
        List<GoalDay> goalDays = goalDayRepository.findByGoalId(goalId);
        StringBuilder days = new StringBuilder();
        for (GoalDay goalDay : goalDays) {
            days.append(goalDay.getDayOfWeek().toString()).append(",");
        }
        // 마지막 콤마 제거
        if (days.length() > 0) {
            days.setLength(days.length() - 1);
        }

        goal.setStatus(GoalStatus.COMPLETE);
        goal.setUpdatedAt(LocalDateTime.now());
        goalRepository.save(goal);

        GoalAchievements goalAchievements = new GoalAchievements();
        goalAchievements.setUser(authUser);
        goalAchievements.setGoal(goal);
        goalAchievements.setName(goal.getName());
        goalAchievements.setTargetCount(goal.getTargetCount());
        goalAchievements.setAchievedCount(goal.getAchievedCount());
        goalAchievements.setStartDate(goal.getStartDate());
        goalAchievements.setEndDate(goal.getEndDate());
        goalAchievements.setDays(days.toString()); // day
        goalAchievements.setPointsEarned(totalEarnedPoints); //TODO : 포인트 로직 완료시 로직 넣기
        goalAchievementsRepository.save(goalAchievements);
        return goal;
    }

    // 목표 총 수행 횟수 계산 메서드 (targetCount)
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

    // 전체목표에서 달력에 사용하는 날짜값 계산기
    @Transactional
    public List<LocalDate> DateRangeCalculator(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기
        List<LocalDate> dateList = new ArrayList<>(); // 반환할 날짜 리스트

        // 목표의 시작일 가져오기
        LocalDate startDate = goal.getStartDate();

        // 오늘이 속한 주의 시작일 (일요일)
        LocalDate thisWeekStart = today.minusDays(today.getDayOfWeek().getValue());

        // startDate가 오늘 이후라면, startDate가 속한 주의 시작일을 기준으로 설정
        LocalDate baseWeekStart;
        if (startDate.isAfter(today)) {
            baseWeekStart = startDate.minusDays(startDate.getDayOfWeek().getValue());
        } else {
            baseWeekStart = thisWeekStart; // startDate가 오늘 이전이거나 오늘 포함인 경우
        }

        // 저번 주(일요일~토요일) 추가
        addWeek(dateList, baseWeekStart.minusWeeks(1));
        // 이번 주(일요일~토요일) 추가
        addWeek(dateList, baseWeekStart);

        System.out.println("최종 날짜 리스트 크기: " + dateList.size());
        return dateList;
    }

    /**
     * 특정 주(일요일~토요일)의 날짜들을 리스트에 추가하는 메서드
     * @param dateList 날짜 리스트
     * @param weekStart 해당 주의 시작일 (일요일)
     */
    private void addWeek(List<LocalDate> dateList, LocalDate weekStart) {
        for (int i = 0; i < 7; i++) {
            dateList.add(weekStart.plusDays(i));
        }
    }

}





