package com.swyp.goal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.goal.entity.DayOfWeek;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.service.GoalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GoalRestController {

    private final GoalService goalService;

    

    //목표 생성, 프론트에서 StatusCheck는 임시저장(DRAFT) 또는 활성화(ACTIVE)를 선택해서 넘겨야 함, selectedDays는 요일 선택 체크박스에서 넘어오는 값
    //Mon, Tue, Wed, Thu, Fri, Sat, Sun 이렇게 넘어옴
    @PostMapping("/v1/goals/create")
    public ResponseEntity<?> createGoal(@ModelAttribute Goal goal,
                                        @RequestParam("status") String statusCheck,
                                        @RequestParam("days") List<DayOfWeek> selectedDays) {
        try {
            Goal createGoal = goalService.createGoal(goal, statusCheck, selectedDays);
            return new ResponseEntity<>(createGoal, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //전체 목표 조회
    @GetMapping("/v1/goals/check")
    public ResponseEntity<?> getGoalList(@RequestParam("userId") Long userId) {
        
    	try {
    	List<Goal> goalList = goalService.getGoalList(userId);
        return new ResponseEntity<>(goalList, HttpStatus.OK);
    	} catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    	}catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    

     //목표 1개 상세 목표 조회 (목표 상세조회)
    @GetMapping("/v1/goals/check/{goalId}")
    public ResponseEntity<Goal> getGoalDetail(@PathVariable("goalId") Long goalId){
        Goal goal = goalService.getGoalDetail(goalId);
        return new ResponseEntity<Goal>(goal,HttpStatus.OK); //JSON형식으로 데이터 보냄
    }
         //임시저장된 목표조회 
    @GetMapping("/v1/goals/{userId}/check/draft")
    public ResponseEntity<List<Goal>> getGoalDraft(@PathVariable("userId") Long userId) {
        List<Goal> goalListDraft = goalService.getDraftGoalList(userId);
        return new ResponseEntity<>(goalListDraft, HttpStatus.OK);
    }

    //목표 상태 변경 (ACTIVE, COMPLETE, DRAFT), 프론트에서 status를 보내줘야함
    @PostMapping("/v1/goals/{goalID}/activate")
    public ResponseEntity<Goal> updateGoalStatus(@PathVariable("goalId") Long goalId, @RequestParam("status") String status) {
        Goal updatedGoal = goalService.updateGoalStatus(goalId, status);
        return new ResponseEntity<>(updatedGoal, HttpStatus.OK);
    }


     //임시저장된 목표 수정 (목표조회후 목표선택하여 goal에 보내줘야한다.
    @PatchMapping("/v1/goals/{goalID}/draft")
    public ResponseEntity<Goal> updateGoalDraft(@PathVariable("goalId") Long goalId, @ModelAttribute Goal goal){
        Goal updatedGoal = goalService.updateGoal(goalId,goal);
        return new ResponseEntity<>(updatedGoal,HttpStatus.OK);
    }

    //목표 삭제 ( 임시저장 또는 활성화 목표만 삭제 가능) , GoalDay삭제는 DB에 ON DELETE CASCADE로 인해 연관된 goal 삭제시 자동삭제
    @PostMapping("/v1/goals/{goalID}/delete")
    public ResponseEntity<Void> deleteGoal(@PathVariable("goalId") Long goalId){
        goalService.deleteGoal(goalId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    //목표 1차인증 (위치 조회후 100m 이내시 1차인증 완료 ), 같은 목표는 하루에 한번만 인증 가능 , 인증시 achieved_count = achieved_count+1 
    @PostMapping("/v1/goals/{goalId}/achieve")
    public ResponseEntity<?> GoalAchievementResponse(
            @PathVariable("goalId") Long goalId,
            @RequestParam("userId") Long userId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude) {
        try {
        	boolean verify = goalService.validateGoalAchievement(userId, goalId, latitude, longitude);//TRUE:성공,FALSE:실패
            return new ResponseEntity<>(verify, HttpStatus.OK); 
        }catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>("예상치 못한 에러", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //목표 complete 후 목표 달성 기록 테이블에 저장
    @PostMapping("/v1/goals/{goalID}/complete")
    public ResponseEntity<Goal> updateGoalStatusToComplete(@PathVariable("goalId") Long goalId){
        Goal updatedGoal = goalService.updateGoalStatusToComplete(goalId);
        return new ResponseEntity<>(updatedGoal, HttpStatus.OK);
    }

    

} 