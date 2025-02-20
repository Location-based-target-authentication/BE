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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "목표", description = "목표 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GoalRestController {

    private final GoalService goalService;

    //목표 생성, 프론트에서 StatusCheck는 임시저장(DRAFT) 또는 활성화(ACTIVE)를 선택해서 넘겨야 함, selectedDays는 요일 선택 체크박스에서 넘어오는 값
    //Mon, Tue, Wed, Thu, Fri, Sat, Sun 이렇게 넘어옴
    @PostMapping("/v1/goals/create")
    public ResponseEntity<Goal> createGoal(@ModelAttribute Goal goal,
                                            @RequestParam("status") String statusCheck,
                                            @RequestParam("days") List<DayOfWeek> selectedDays) {
        Goal createGoal = goalService.createGoal(goal, statusCheck, selectedDays);
        return new ResponseEntity<>(createGoal, HttpStatus.CREATED); // JSON 형식으로 데이터 반환
    }

    //전체 목표 조회
    @GetMapping("/v1/goals/check")
    public ResponseEntity<List<Goal>> getGoalList(@RequestParam("userId") Long userId) {
        List<Goal> goalList = goalService.getGoalList(userId);
        return new ResponseEntity<>(goalList, HttpStatus.OK);
    }

     //목표 1개 상세 목표 조회 (목표 상세조회)1
    @PostMapping("/v1/goals/check/{goalId}")
    public ResponseEntity<Goal> getGoalDetail(@PathVariable("goalId") Long goalId){
        Goal goal = goalService.getGoalDetail(goalId);
        return new ResponseEntity<Goal>(goal,HttpStatus.ACCEPTED); //JSON형식으로 데이터 보냄
    }
    //임시저장된 목표조회
    @GetMapping("/v1/goals/{userId}/check/draft")
    public ResponseEntity<List<Goal>> getGoalDraft(@PathVariable("userId") Long userId) {
        List<Goal> goalListDraft = goalService.getDraftGoalList(userId);
        return new ResponseEntity<>(goalListDraft, HttpStatus.OK);
    }

    //목표 상태 변경 (ACTIVE, COMPLETE, DRAFT), 프론트에서 status를 보내줘야함
    @Operation(summary = "목표 활성화")
    @PostMapping("/v1/goals/{goalID}/activate")
    public ResponseEntity<Goal> updateGoalStatus(
        @Parameter(description = "목표 ID") @PathVariable Long goalID,
        @RequestParam String status
    ) {
        Goal updatedGoal = goalService.updateGoalStatus(goalID, status);
        return new ResponseEntity<>(updatedGoal, HttpStatus.OK);
    }


     //임시저장된 목표 수정 (목표조회후 목표선택하여 goal에 보내줘야함)
    @PatchMapping("/v1/goals/{goalID}/draft")
    public ResponseEntity<Goal> updateGoalDraft(@PathVariable("goalID") Long goalID, @ModelAttribute Goal goal){
        Goal updatedGoal = goalService.updateGoal(goalID,goal);
        return new ResponseEntity<>(updatedGoal,HttpStatus.OK);
    }

    //목표 삭제 ( 임시저장 또는 활성화 목표만 삭제 가능)
    @PostMapping("/v1/goals/{goalID}/delete")
    public ResponseEntity<Void> deleteGoal(@PathVariable("goalID") Long goalID){
        goalService.deleteGoal(goalID);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    

} 