package com.swyp.goal.controller;

import java.time.LocalDate;
import java.util.ArrayList;
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

import com.swyp.goal.dto.GoalDetailDto;
import com.swyp.goal.dto.GoalHomeResponseDto;
import com.swyp.goal.entity.DayOfWeek;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.entity.GoalDay;
import com.swyp.goal.repository.GoalAchievementsLogRepository;
import com.swyp.goal.repository.GoalDayRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.goal.service.GoalService;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.social_login.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@Tag(name = "목표", description = "목표 관련 API")
@RequiredArgsConstructor
public class GoalRestController {

    private final GoalService goalService;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final GoalPointHandler goalPointHandler;
    private final GoalAchievementsLogRepository goalAchievementLogRepository;
    private final GoalDayRepository goalDayRepository;

    // 목표 home
    @Operation(
    	    summary = "목표 home",
    	    description = "userId를 넘겨 목표 home에서 쓸 데이터를 가져옵니다.",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공",
    	            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GoalHomeResponseDto.class))
    	        ),
    	        @ApiResponse(
    	            responseCode = "500",
    	            description = "서버 내부 오류",
    	            content = @Content(
    	                mediaType = "application/json",
    	                schema = @Schema(example = "{\"String\": \"예상치 못한 오류\"}")
    	            )
    	        )
    	    }
    	)
    @GetMapping("/v1/goals/{userId}")
    public ResponseEntity<?> goalHome(@PathVariable("userId")Long userId){
    	try {
    	List<Goal>goals = goalService.getGoalList(userId);
    	List<GoalHomeResponseDto> goalHomeDtoList  = new ArrayList<>();
    	
    	for (Goal goal : goals) {
    		boolean isAchievedToday = goalAchievementLogRepository.existsByUserIdAndGoalIdAndAchievedAtAndAchievedSuccess( // 오늘 목표 인증을했는지 했으면 true, 안했으면 false
                    userId, goal.getId(), LocalDate.now(),true);
    		GoalHomeResponseDto dto = new GoalHomeResponseDto(goal.getName(), goal.getStartDate(), goal.getEndDate(),goal.getStatus().name(), isAchievedToday);
    		goalHomeDtoList.add(dto);
    	}
    	return new ResponseEntity<>(goalHomeDtoList,HttpStatus.OK);
    	}catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR); // 적절한 HTTP 상태 코드로 변경
        }
    	
    }
    
    
    
    
    //목표 생성, 프론트에서 StatusCheck는 임시저장(DRAFT) 또는 활성화(ACTIVE)를 선택해서 넘겨야 함, selectedDays는 요일 선택 체크박스에서 넘어오는 값
    //Mon, Tue, Wed, Thu, Fri, Sat, Sun 이렇게 넘어옴
    @Operation(
    	    summary = "목표 생성",
    	    description = "목표 생성을 위해 여러 값을 넘겨야 됩니다.",
    	    responses = {
    	    	@ApiResponse(
    	     	         responseCode = "200",
    	     	         description = "성공",
    	     	         content = @Content(mediaType = "application/json",schema = @Schema(implementation = Goal.class))
    	     	     ),
    	        @ApiResponse(
    	            responseCode = "400",
    	            description = "조건 설정 오류 (ex.목표는 최대 3개까지만 생성할 수 있습니다.  ,목표 이름은 필수 입력 사항입니다."
    	            		+ "  ,목표 이름은 2~20자 이내여야 합니다.  ,시작일은 오늘 이후여야 합니다.,종료일은 시작일 이후여야 합니다."
    	            		+ "  ,종료일은 시작일 기준으로 최소 1주일 뒤여야 합니다.  ,종료일은 시작일 기준으로 최대 3개월 이내여야 합니다.) ",
    	            content = @Content(
    	                mediaType = "application/json",
    	                schema = @Schema(example = "{\"String\": \"목표는 최대 3개까지만 생성할 수 있습니다.\"}")
    	            )
    	        ),
    	        @ApiResponse(
        	            responseCode = "500",
        	            description = "서버 내부 오류",
        	            content = @Content(
        	                mediaType = "application/json",
        	                schema = @Schema(example = "{\"String\": \"Internal server error\"}")
        	            )
        	        )
    	        
    	    }
    	)
    @PostMapping("/v1/goals/create")
    public ResponseEntity<?> createGoal(@ModelAttribute Goal goal,
                                        @RequestParam("status") String statusCheck,
                                        @RequestParam("days") List<DayOfWeek> selectedDays) {
        try {
            Goal createGoal = goalService.createGoal(goal, statusCheck, selectedDays);
            return new ResponseEntity<>("목표 생성 성공", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    //전체 목표 조회
    @Operation(
    	    summary = "전체 목표 조회",
    	    description = "userId를 이용해 전체 목표 조회를 합니다.",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공, (List로 전달)",
    	            content = @Content(
    	                mediaType = "application/json",
    	                array = @ArraySchema(schema = @Schema(implementation = Goal.class))
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "500",
    	            description = "서버 내부 오류",
    	            content = @Content(
    	                mediaType = "application/json",
    	                schema = @Schema(example = "{\"String\": \"Internal server error\"}")
    	            )
    	        )
    	    }
    	)
    @GetMapping("/v1/goals/check")
    public ResponseEntity<?> getGoalList(@RequestParam("userId") Long userId) {
        
    	try {
    	List<Goal> goalList = goalService.getGoalList(userId);
        return new ResponseEntity<>(goalList, HttpStatus.OK);
    	} catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    
    
     //목표 1개 상세 목표 조회 (목표 상세조회)
    @Operation(
    	    summary = "목표 상세 조회",
    	    description = "userId를 이용해 목표 상세조회를 합니다.",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공",
    	            content = @Content(mediaType = "application/json",schema = @Schema(implementation = GoalHomeResponseDto.class))
    	        )
    	    }
    	)
    @GetMapping("/v1/goals/check/{goalId}")
    public ResponseEntity<?> getGoalDetail(@PathVariable("goalId") Long goalId){
        Goal goal = goalService.getGoalDetail(goalId);
        List<GoalDay> goalDays = goalDayRepository.findByGoalId(goalId);
        
        // 요일을 저장할 StringBuilder 생성
        StringBuilder dayOfWeekStr = new StringBuilder();
        
        // goalDays 리스트를 순회하면서 dayOfWeek 값을 추가
        for (int i = 0; i < goalDays.size(); i++) {
            dayOfWeekStr.append(goalDays.get(i).getDayOfWeek());
            
            // 마지막 요소가 아니라면 쉼표 추가
            if (i < goalDays.size() - 1) {
                dayOfWeekStr.append(",");
            }
        }
        
        GoalDetailDto dto = new GoalDetailDto(goal.getId(), goal.getName(),goal.getStatus().name(),goal.getStartDate(),goal.getEndDate(),goal.getLocationName(),
        		goal.getLatitude(),goal.getLongitude(),goal.getTargetCount(),goal.getAchievedCount(),dayOfWeekStr.toString());

        return new ResponseEntity<>(dto,HttpStatus.OK); //JSON형식으로 데이터 보냄
    }
    
         //임시저장된 목표조회 ( 사용 x )
    @GetMapping("/v1/goals/{userId}/check/draft")
    public ResponseEntity<List<Goal>> getGoalDraft(@PathVariable("userId") Long userId) {
        List<Goal> goalListDraft = goalService.getDraftGoalList(userId);
        return new ResponseEntity<>(goalListDraft, HttpStatus.OK);
    }

    
    //목표 상태 변경 (ACTIVE, COMPLETE, DRAFT), 프론트에서 status를 보내줘야함
    @Operation(
    	    summary = "목표 상태(Status) 변경 ",
    	    description = "userId를 이용해 목표 상세조회를 합니다.status(ACTIVE, COMPLETE, DRAFT)를 보내줘야함",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공",
    	            content = @Content(
    	            	 mediaType = "application/json",
        	             schema = @Schema(example = "{\"String\": \"목표 상태 변경 성공\"}")
    	            )
    	        ),
    	
    	    }
    	)
    @PostMapping("/v1/goals/{goalId}/activate")
    public ResponseEntity<?> updateGoalStatus(@PathVariable("goalId") Long goalId, @RequestParam("status") String status) {
        Goal updatedGoal = goalService.updateGoalStatus(goalId, status);
        
        return new ResponseEntity<>("목표 상태 변경 성공", HttpStatus.OK);
    }


     //임시저장된 목표 수정 (목표조회후 목표선택하여 goal에 보내줘야한다).
    @Operation(
    	    summary = "임시저장된 목표 수정 ",
    	    description = "임시저장된 목표를 목표상세에서 goalId를 통해 불러온후 수정한 데이터 전부를 Goal 형식에 맞춰 보내야한다. 넘기는값은 goalId + goal",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공",
    	            content = @Content(
    	            	 mediaType = "application/json",
        	             schema = @Schema(example = "{\"String\": \"임시 저장된 목표 수정 완료\"}")
    	            )
    	        ),
    	        @ApiResponse(
        	            responseCode = "400",
        	            description = "임시저장 목표만 수정할 수 있습니다.",
        	            content = @Content(
        	                mediaType = "application/json",
        	                schema = @Schema(example = "{\"String\": \"임시저장 목표만 수정할 수 있습니다.\"}")
        	            )
        	        ),
        	    @ApiResponse(
            	        responseCode = "500",
            	        description = "서버 내부 오류",
            	        content = @Content(
            	            mediaType = "application/json",
            	            schema = @Schema(example = "{\"String\": \"Internal server error\"}")
            	        )
            	    )
    	    }
    	)
    @PatchMapping("/v1/goals/{goalId}/draft")
    public ResponseEntity<?> updateGoalDraft(@PathVariable("goalId") Long goalId, @ModelAttribute Goal goal){
        try {
    	Goal updatedGoal = goalService.updateGoal(goalId,goal);
        return new ResponseEntity<>("임시 저장된 목표 수정 완료",HttpStatus.OK);
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 	
        }catch (Exception e) {
        	return new ResponseEntity<>("Internal server error", HttpStatus.BAD_REQUEST);
        }
    }
    
    
    
    @Operation(
    	    summary = "목표 삭제 ",
    	    description = "목표삭제 ( 임시저장 또는 활성화 목표만 삭제 가능)",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공",
    	            content = @Content(
    	            	 mediaType = "application/json",
        	             schema = @Schema(example = "{\"String\": \"목표 삭제 성공\"}")
    	            )
    	        ),
    	        @ApiResponse(
        	            responseCode = "400",
        	            description = "임시저장 또는 활성화 목표만 삭제할 수 있습니다.",
        	            content = @Content(
        	                mediaType = "application/json",
        	                schema = @Schema(example = "{\"String\": \"임시저장 또는 활성화 목표만 삭제할 수 있습니다.\"}")
        	            )
        	        ),
        	    @ApiResponse(
            	        responseCode = "500",
            	        description = "서버 내부 오류",
            	        content = @Content(
            	            mediaType = "application/json",
            	            schema = @Schema(example = "{\"String\": \"Internal server error\"}")
            	        )
            	    )
    	    }
    	)
    //목표 삭제 ( 임시저장 또는 활성화 목표만 삭제 가능) , GoalDay삭제는 DB에 ON DELETE CASCADE로 인해 연관된 goal 삭제시 자동삭제
    @PostMapping("/v1/goals/{goalId}/delete")
    public ResponseEntity<?> deleteGoal(@PathVariable("goalId") Long goalId){
        try {
    	goalService.deleteGoal(goalId);
        return new ResponseEntity<>("목표 삭제 성공",HttpStatus.NO_CONTENT);
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 	
        }catch (Exception e) {
        	return new ResponseEntity<>("Internal server error", HttpStatus.BAD_REQUEST);
        }
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
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //목표 complete 후 목표 달성 기록 테이블에 저장
    @PostMapping("/v1/goals/{goalId}/complete")
    public ResponseEntity<?> updateGoalStatusToComplete(@PathVariable("goalId") Long goalId){
        try {
    	Goal updatedGoal = goalService.updateGoalStatusToComplete(goalId);
        return new ResponseEntity<>("목표 달성 완료 (목표Status:COMPLETE로변경)", HttpStatus.OK);
        }catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    

//    //(포인트) 목표 달성
//    @PostMapping("/v1/goals/{goalId}/complete")
//    public ResponseEntity<String> completeGoal(
//            @PathVariable("goalId") Long goalId,
//            @RequestParam("userId") Long userId,
//            @RequestParam("isSelectedDay") boolean isSelectedDay) {
//        // 사용자 정보 조회
//        AuthUser authUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//        // 목표 정보 조회
//        Goal goal = goalRepository.findById(goalId)
//                .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
//        // 선택된 요일 정보 조회
//        List<DayOfWeek> selectedDays = goalService.getSelectedDays(goalId);
//        // 목표 완료 핸들러 호출
//        goalPointHandler.handleGoalCompletion(authUser, goal, selectedDays, isSelectedDay);
//        return new ResponseEntity<>("목표 달성 및 포인트 적립 완료", HttpStatus.OK);
//    }

} 