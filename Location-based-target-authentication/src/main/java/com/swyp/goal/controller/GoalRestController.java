package com.swyp.goal.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.goal.dto.CompleteResponseDto;
import com.swyp.goal.dto.GoalAllSearchDto;
import com.swyp.goal.dto.GoalCompleteDto;
import com.swyp.goal.dto.GoalCreateRequest;
import com.swyp.goal.dto.GoalDateDto;
import com.swyp.goal.dto.GoalDetailDto;
import com.swyp.goal.dto.GoalHomeResponseDto;
import com.swyp.goal.dto.GoalUpdateDto;
import com.swyp.goal.entity.Goal;
import com.swyp.goal.entity.GoalAchievements;
import com.swyp.goal.entity.GoalAchievementsLog;
import com.swyp.goal.entity.GoalDay;
import com.swyp.goal.repository.GoalAchievementsLogRepository;
import com.swyp.goal.repository.GoalDayRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.goal.service.GoalScheduledService;
import com.swyp.goal.service.GoalService;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
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
    private final PointService pointService;
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
    	            content = @Content(mediaType = "application/json", schema = @Schema(
    	                    implementation = GoalHomeResponseDto.class
    	                ))
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
    		
    		// goalDays : 요일 String값으로 가공
    		List<GoalDay> goalDays = goalDayRepository.findByGoalId(goal.getId());
            StringBuilder days = new StringBuilder();
            for (GoalDay goalDay : goalDays) {
                days.append(goalDay.getDayOfWeek().toString()).append(",");
            }
            // 마지막 콤마 제거
            if (days.length() > 0) {
                days.setLength(days.length() - 1);
            }
            
    		GoalHomeResponseDto dto = new GoalHomeResponseDto(goal.getId(), userId, goal.getName(), goal.getStartDate(), goal.getEndDate(), goal.getStatus().name(), isAchievedToday, days.toString());
    		goalHomeDtoList.add(dto);
    	}	
    	return new ResponseEntity<>(goalHomeDtoList,HttpStatus.OK);
    	}catch (Exception e) {
            return new ResponseEntity<>(new CompleteResponseDto("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR); // 적절한 HTTP 상태 코드로 변경
        }
    	
    }

    
    //목표 생성, 프론트에서 StatusCheck는 임시저장(DRAFT) 또는 활성화(ACTIVE)를 선택해서 넘겨야 함, selectedDays는 요일 선택 체크박스에서 넘어오는 값
    //Mon, Tue, Wed, Thu, Fri, Sat, Sun 이렇게 넘어옴
    @Operation(
    	    summary = "목표 생성",
    	    description = "목표 생성을 위해 여러 값을 넘겨야 됩니다.",
    	    responses = {
    	    	@ApiResponse(
    	     	         responseCode = "201",
    	     	         description = "성공",
    	     	         content = @Content(mediaType = "application/json",schema = @Schema(example = "{\"String\": \"목표 생성 성공\"}"))
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
    public ResponseEntity<?> createGoal(@RequestBody GoalCreateRequest request) {
        try {
            Goal createdGoal = goalService.createGoal(request);

            // (포인트) 생성된 목표의 userId를 이용해 사용자를 조회
            AuthUser authUser = userRepository.findById(createdGoal.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            int updatedPoints = pointService.getUserPoints(authUser);

            // 응답 데이터: 목표 정보 & 잔여 포인트
            Map<String, Object> response = new HashMap<>();
            response.put("goal", createdGoal);
            response.put("totalPoints", updatedPoints);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new CompleteResponseDto("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    //전체 목표 조회
    @Operation(
    	    summary = "전체 목표 조회",
    	    description = "userId를 이용해 전체 목표 조회를 합니다.",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공,",
    	            content = @Content(
    	                mediaType = "application/json",
    	                array = @ArraySchema(schema = @Schema(implementation = GoalAllSearchDto.class))
    	            )
    	        ),
    	        @ApiResponse(
    	            responseCode = "500",
    	            description = "서버 내부 오류",
    	            content = @Content(
    	                mediaType = "application/json",
    	                schema = @Schema(example = "{\"String\": \"e.getMessage()\"}")
    	            )
    	        )
    	    }
    	)
    @GetMapping("/v1/goals/check")
    public ResponseEntity<?> getGoalList(@RequestParam("userId") Long userId) {
    	try {
    	List<Goal> goalList = goalService.getGoalList(userId);
    	List<GoalAllSearchDto> goalAllDto = new ArrayList<>();
    	for(Goal goal : goalList) {
    		List<LocalDate> calender = goalService.DateRangeCalculator(goal.getId());
    		System.out.println(calender);
    		List<GoalDateDto> goalDateDto = new ArrayList<>();
    		List<GoalAchievementsLog> logs = goalAchievementLogRepository.findByGoalIdAndAchievedSuccessIsTrue(goal.getId());


    		 for (GoalAchievementsLog log : logs) {
    	            GoalDateDto dto = new GoalDateDto(log.getAchievedAt(), log.isAchievedSuccess());
    	            goalDateDto.add(dto);
    	        }
    		 
    		// goalDays : 요일 String값으로 가공
     		List<GoalDay> goalDays = goalDayRepository.findByGoalId(goal.getId());
             StringBuilder days = new StringBuilder();
             for (GoalDay goalDay : goalDays) {
                 days.append(goalDay.getDayOfWeek().toString()).append(",");
             }
             // 마지막 콤마 제거
             if (days.length() > 0) {
                 days.setLength(days.length() - 1);
             }
    		 
    		 GoalAllSearchDto dto = new GoalAllSearchDto(goal.getId(),goal.getUserId(),goal.getName(),goal.getStatus(),goal.getStartDate(),goal.getEndDate(),goal.getLocationName(),goal.getLatitude(),goal.getLongitude(),goal.getRadius(),goal.getTargetCount(),goal.getAchievedCount(),
                     goalDateDto,  // 인증된 날짜들
                     calender
                     ,days.toString());   // 날짜 값들
             
    		 goalAllDto.add(dto);
    		 
    	}
        return new ResponseEntity<>(goalAllDto, HttpStatus.OK);
    	} catch (Exception e) {
            return new ResponseEntity<>(new CompleteResponseDto("e.getMessage()"), HttpStatus.INTERNAL_SERVER_ERROR);
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
    	            content = @Content(mediaType = "application/json",schema = @Schema(implementation = GoalDetailDto.class))
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
    
    
    // 완료 목표 전체 조회
    @Operation(
    	    summary = "완료 목표 전체 조회",
    	    description = "userId를 이용해 완료 목표 전체조회를 합니다.",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공",
    	            content = @Content(mediaType = "application/json",schema = @Schema(implementation = GoalCompleteDto.class))
    	        )
    	    }
    	)
    @GetMapping("/v1/goals/check/complete/{userId}")
    public ResponseEntity<?> getGoalCompleteList(@PathVariable("userId") Long userId){
    	List<GoalAchievements> goalAchievements = goalService.getGoalAchievementsList(userId);
    	List<GoalCompleteDto> goalCompleteDtoList = new ArrayList<>();
    	for(GoalAchievements goalAchievement : goalAchievements) {
    		
    		GoalCompleteDto goalCompleteDto = new GoalCompleteDto(goalAchievement.getName(),goalAchievement.getTargetCount(),goalAchievement.getAchievedCount(),goalAchievement.getPointsEarned()
    				,goalAchievement.getStartDate(),goalAchievement.getEndDate(),goalAchievement.getDays());
    		
    		goalCompleteDtoList.add(goalCompleteDto);
    	}
    	
    	return new ResponseEntity<>(goalCompleteDtoList,HttpStatus.OK);
    	
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
        System.out.println("도달");
        
        return new ResponseEntity<>(new CompleteResponseDto("목표 상태 변경 성공"), HttpStatus.OK);
    }


     //임시저장된 목표 수정 (목표조회후 목표선택하여 goalid와 GoalUpdateDto을 보내줘야한다).
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
    public ResponseEntity<?> updateGoalDraft(@PathVariable("goalId") Long goalId, @RequestBody GoalUpdateDto goal){
        try {
    	Goal updatedGoal = goalService.updateGoal(goalId,goal);
        return new ResponseEntity<>(new CompleteResponseDto("임시 저장된 목표 수정 완료"),HttpStatus.OK);
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST); 	
        }catch (Exception e) {
        	return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
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
            	            schema = @Schema(example = "{\"message\": \"Internal server error\"}")
            	        )
            	    )
    	    }
    	)
    //목표 삭제  , GoalDay삭제는 DB에 ON DELETE CASCADE로 인해 연관된 goal 삭제시 자동삭제
    @PostMapping("/v1/goals/{goalId}/delete")
    public ResponseEntity<?> deleteGoal(@PathVariable("goalId") Long goalId){
        try {
    	goalService.deleteGoal(goalId);
        return new ResponseEntity<>(new CompleteResponseDto("목표 삭제 성공"),HttpStatus.OK);
        }catch (IllegalArgumentException e) {
        	System.out.println(e.getMessage());
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST); 	
        }catch (Exception e) {
        	return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Operation(
        summary = "목표 인증",
        description = "목표 인증을 위한 API입니다. 현재 위치가 목표 위치와 100m 이내일 경우 인증이 성공합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "성공",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{"
                        + "\"achievementStatus\": \"성공\","
                        + "\"totalPoints\": 130,"
                        + "\"bonusPoints\": 30,"
                        + "\"message\": \"목표 인증에 성공했습니다.\""
                        + "}")
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "인증 실패",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{"
                        + "\"achievementStatus\": \"실패\","
                        + "\"totalPoints\": 100,"
                        + "\"bonusPoints\": 0,"
                        + "\"message\": \"목표 위치가 현재 위치와 100m 이상 차이가 있거나, 오늘 이미 인증했습니다.\""
                        + "}")
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"message\": \"서버 내부 오류가 발생했습니다.\"}"))
            )
        }
    )
    @Parameters({
        @Parameter(
            name = "goalId",
            description = "목표 ID",
            required = true,
            example = "1"
        ),
        @Parameter(
            name = "userId",
            description = "사용자 ID",
            required = true,
            example = "1"
        ),
        @Parameter(
            name = "latitude",
            description = "현재 위도",
            required = true,
            example = "37.123456"
        ),
        @Parameter(
            name = "longitude",
            description = "현재 경도",
            required = true,
            example = "127.123456"
        )
    })

    //목표 1차인증 (위치 조회후 100m 이내시 1차인증 완료 ), 같은 목표는 하루에 한번만 인증 가능 , 인증시 achieved_count = achieved_count+1 
    @PostMapping("/v1/goals/{goalId}/achieve")
    public ResponseEntity<?> GoalAchievementResponse(
            @PathVariable("goalId") Long goalId,
            @RequestParam("userId") Long userId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude
    ) {
        try {
            // 1. 사용자 확인
            AuthUser authUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            // 2. 목표 확인
            Goal goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
            
            // 3. 기존 포인트 저장
            int previousPoints = pointService.getUserPoints(authUser);
            
            // 4. 목표 위치 검증
            boolean isVerified = goalService.validateGoalAchievement(userId, goalId, latitude, longitude);
            
            // 5. 응답 데이터 준비
            Map<String, Object> response = new HashMap<>();
            
            if (!isVerified) {
                response.put("achievementStatus", "실패");
                response.put("totalPoints", previousPoints);
                response.put("bonusPoints", 0);
                response.put("message", "목표 위치가 현재 위치와 100m 이상 차이가 있거나, 오늘 이미 인증했습니다.");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            
            // 6. 포인트 처리
            boolean isSelectedDay = true; // 선택된 요일 여부 확인 로직 필요시 추가
            goalPointHandler.handleDailyAchievement(authUser, goal, isSelectedDay);
            int afterDailyPoints = pointService.getUserPoints(authUser);
            
            // 7. 보너스 포인트 처리
            goalPointHandler.handleWeeklyGoalCompletion(authUser, goal);
            int afterBonusPoints = pointService.getUserPoints(authUser);
            
            // 8. 최종 응답 생성
            response.put("achievementStatus", "성공");
            response.put("totalPoints", afterBonusPoints);
            response.put("bonusPoints", afterBonusPoints - afterDailyPoints);
            response.put("message", "목표 인증에 성공했습니다.");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new CompleteResponseDto("서버 내부 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    //목표 complete 후 목표 달성 기록 테이블에 저장.
    @Operation(
    	    summary = "목표 완료 (Status:Complete)",
    	    description = "목표 complete 후 목표 달성 기록 테이블에 저장  ",
    	    responses = {
    	        @ApiResponse(
    	            responseCode = "200",
    	            description = "성공",
    	            content = @Content(
    	            	 mediaType = "application/json",
    	            	 schema = @Schema(example = "{\"String\": \"목표 달성 완료\"}")
    	            )
    	        ),
    	        @ApiResponse(
        	            responseCode = "400",
        	            description = "Status가 ACTIVE인 애들만 완료 처리가능,   목표달성 횟수보다 실제 목표 달성횟수가 커야지 Complete가능 ",
        	            content = @Content(
        	                mediaType = "application/json",
        	                schema = @Schema(example = "{\"String\": \"활성화된 목표만 완료 처리할 수 있습니다.,    지정된 목표 달성 횟수를 채우지 못하셨습니다.,  존재하지 않는 목표입니다.\"}")
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
    @PostMapping("/v1/goals/{goalId}/complete")
    public ResponseEntity<?> updateGoalStatusToComplete(@PathVariable("goalId") Long goalId,@RequestParam("userId") Long userId,
            @RequestParam("isSelectedDay") boolean isSelectedDay) {
    	try {
        AuthUser authUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
        // 목표 상태 COMPLETE로 변경 (목표 횟수 달성 시)
        Goal updatedGoal = goalService.updateGoalStatusToComplete(goalId, authUser.getUserId(), isSelectedDay);
        goalRepository.save(updatedGoal);
        return new ResponseEntity<>(new CompleteResponseDto("목표 달성 완료"), HttpStatus.OK);
    	}catch (IllegalStateException e) {
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    




}
