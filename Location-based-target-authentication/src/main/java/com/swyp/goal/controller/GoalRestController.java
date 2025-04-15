package com.swyp.goal.controller;

import java.time.LocalDate;
import java.util.*;
import java.net.URI;
import java.util.stream.Collectors;

import com.swyp.goal.entity.*;
import com.swyp.point.enums.PointType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swyp.goal.dto.CompleteResponseDto;
import com.swyp.goal.dto.GoalAchieveRequestDto;
import com.swyp.goal.dto.GoalAllSearchDto;
import com.swyp.goal.dto.GoalCompleteDto;
import com.swyp.goal.dto.GoalCreateRequest;
import com.swyp.goal.dto.GoalDateDto;
import com.swyp.goal.dto.GoalDetailDto;
import com.swyp.goal.dto.GoalHomeResponseDto;
import com.swyp.goal.dto.GoalUpdateDto;
import com.swyp.goal.repository.GoalAchievementsLogRepository;
import com.swyp.goal.repository.GoalDayRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.goal.repository.GoalAchievementsRepository;
import com.swyp.goal.service.GoalScheduledService;
import com.swyp.goal.service.GoalService;
import com.swyp.point.service.GoalPointHandler;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import com.swyp.global.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
    private final JwtUtil jwtUtil;
    private final GoalAchievementsRepository goalAchievementsRepository;
 
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
    public ResponseEntity<?> goalHome(@PathVariable("userId") Long userId){
    	try {
    	List<Goal>goals = goalService.getGoalList(userId).stream()
            .filter(goal -> goal.getStatus() == GoalStatus.ACTIVE || goal.getStatus() == GoalStatus.DRAFT)
            .collect(Collectors.toList());
    	List<GoalHomeResponseDto> goalHomeDtoList  = new ArrayList<>();
    	
    	for (Goal goal : goals) {
    		boolean isAchievedToday = goalAchievementLogRepository.existsByUser_IdAndGoal_IdAndAchievedAtAndAchievedSuccess( // 오늘 목표 인증을했는지 했으면 true, 안했으면 false
                    userId, goal.getId(), LocalDate.now(), true);
    		
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
    public ResponseEntity<?> createGoal(@RequestBody GoalCreateRequest request, HttpServletRequest httpRequest) {
        try {
            // JWT 토큰에서 userId 추출
            String bearerToken = httpRequest.getHeader("Authorization");
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("인증 토큰이 필요합니다.");
            }
            String token = bearerToken.substring(7);
            Long tokenUserId = jwtUtil.extractUserId(token);
            
            System.out.println("[GoalRestController] 토큰에서 추출한 userId: " + tokenUserId);
            
            // 사용자 조회
            AuthUser authUser;
            try {
                System.out.println("[GoalRestController] findById 시도: " + tokenUserId);
                Optional<AuthUser> userById = userRepository.findByUserId(tokenUserId);
                if (userById.isPresent()) {
                    System.out.println("[GoalRestController] findById 성공");
                    authUser = userById.get();
                } else {
                    System.out.println("[GoalRestController] findById 실패, findByUserIdEquals 시도: " + tokenUserId);
                    Optional<AuthUser> userByUserId = userRepository.findByUserIdEquals(tokenUserId);
                    if (userByUserId.isPresent()) {
                        System.out.println("[GoalRestController] findByUserIdEquals 성공");
                        authUser = userByUserId.get();
                    } else {
                        System.out.println("[GoalRestController] 모든 조회 실패. tokenUserId=" + tokenUserId);
                        throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                    }
                }
            } catch (Exception e) {
                System.out.println("[GoalRestController] 사용자 조회 중 예외 발생: " + e.getMessage());
                e.printStackTrace();
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
            }
            
            System.out.println("[GoalRestController] 찾은 사용자 - id: " + authUser.getId() + ", userId: " + authUser.getUserId());
            // request의 userId를 AuthUser의 id(PK)로 설정
            request.setUserId(authUser.getId());
            
            // 목표 생성
            Goal createdGoal = goalService.createGoal(request);
            
            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("message", "목표 생성 성공");
            response.put("goal", createdGoal);
            response.put("totalPoints", pointService.getUserPoints(authUser));

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage().contains("포인트 부족") ? 
                "포인트가 부족하여 목표를 생성할 수 없습니다." : 
                "서버 오류가 발생했습니다: " + e.getMessage();
            return new ResponseEntity<>(new CompleteResponseDto(errorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
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
    		List<LocalDate> calender = goalService.DateRangeCalculator(goal.getId()); // 목표 달력을 위한 전체 날짜값(today가 startDate의 주에 속하면 이번 주 + 다음 주 ,  today가 startDate의 주에 속하지 않으면 지난주 + 이번 주) 
    		System.out.println(calender);
    		List<GoalDateDto> goalDateDto = new ArrayList<>(); // 목표 달력을 위한 인증 날짜값
    		
            // 목표의 시작일부터 종료일까지의 모든 날짜를 생성
            LocalDate startDate = goal.getStartDate();
            LocalDate endDate = goal.getEndDate();
            List<LocalDate> allDates = new ArrayList<>();
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                allDates.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }

            // 인증 성공한 날짜들을 Set으로 변환 (속도 떄문에)
            Set<LocalDate> successDates = new HashSet<>();
            List<GoalAchievementsLog> achievementLogs = goalAchievementLogRepository
                    .findByGoal_IdAndAchievedSuccessIsTrue(goal.getId());
            for (GoalAchievementsLog log : achievementLogs) {
                successDates.add(log.getAchievedAt());
            }

            // 모든 날짜에 대해 인증 상태를 확인하여 DTO 생성 , 인증 성공한 날짜 확인 후 인증 날짜 값 추가
            for (LocalDate date : allDates) {
                GoalDateDto dto = new GoalDateDto(date, successDates.contains(date));
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
    		 
    		 GoalAllSearchDto dto = new GoalAllSearchDto(goal.getId(),goal.getId(),goal.getName(),goal.getStatus(),goal.getStartDate(),goal.getEndDate(),goal.getLocationName(),goal.getLatitude(),goal.getLongitude(),goal.getRadius(),goal.getTargetCount(),goal.getAchievedCount(),
                     goalDateDto,  // 인증된 날짜들
                     calender     // 날짜 값들
                     ,days.toString());   
             
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
    	    description = "임시저장된 목표를 목표상세에서 goalId를 통해 불러온후 수정한 데이터 전부를 Goal 형식에 맞춰 보내야한다. ",
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
                        + "\"currentPoints\": 50"
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
                        + "\"currentPoints\": 50"
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
            example = "1",
            in = ParameterIn.PATH
        )
    })

    //목표 1차인증 (위치 조회후 100m 이내시 1차인증 완료 ), 같은 목표는 하루에 한번만 인증 가능 , 인증시 achieved_count = achieved_count+1 
    @PostMapping("/v1/goals/{goalId}/achieve")
    public ResponseEntity<?> GoalAchievementResponse(
            @PathVariable("goalId") Long goalId,
            @RequestBody GoalAchieveRequestDto requestDto
    ) {
        try {
            // 필수 파라미터 검증
            if (requestDto.getUserId() == null) {
                return new ResponseEntity<>(new CompleteResponseDto("사용자 ID가 입력되지 않았습니다."), HttpStatus.BAD_REQUEST);
            }
            if (requestDto.getLatitude() == null || requestDto.getLongitude() == null) {
                return new ResponseEntity<>(new CompleteResponseDto("위치 정보(위도/경도)가 입력되지 않았습니다."), HttpStatus.BAD_REQUEST);
            }
            
        	// 1.위치 검증 성공시 true , 실패시 false 
        	boolean verify = goalService.validateGoalAchievement(requestDto.getUserId(), goalId, requestDto.getLatitude(), requestDto.getLongitude());
        	// 2. 사용자 정보 조회
            AuthUser authUser = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            // 목표 조회
            Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 목표입니다."));
            // 4. 응답값을 담기위한 response
            Map<String, Object> response = new HashMap<>();
			// (포인트) 당일, 보너스, 총 포인트 반환
			int totalPoints = pointService.getTotalPoints(authUser);
            if(verify) { // 위치 검증 성공
				int dailyPoints = goalPointHandler.handleDailyAchievement(authUser, goal, true);
				int bonusPoints = goalPointHandler.handleWeeklyGoalCompletion(authUser, goal);
				response.put("achievementStatus", "성공");
				response.put("totalPoints", totalPoints); // 전체 포인트
				response.put("currentPoints", dailyPoints); // 오늘 획득한 포인트
				response.put("bonusPoints", bonusPoints); // 보너스 포인트
				response.put("message", "목표 인증에 성공했습니다.");
        		goalRepository.save(goal); // goal테이블의 updated_at 업데이트를 위한 save
        		return new ResponseEntity<>(response, HttpStatus.OK);
        	}else { // 위치 검증 실패 
        		response.put("achievementStatus", "실패");
				response.put("totalPoints", totalPoints);
				response.put("currentPoints", 0);
				response.put("bonusPoints", 0);
				response.put("message", "목표 위치가 현재 위치와 100m 이상 차이가 있거나, 오늘 이미 인증했습니다.");
				goalRepository.save(goal);
        		return new ResponseEntity<>(response, HttpStatus.OK);
        	}
		} catch (Exception e) {
		    e.printStackTrace(); // 로그에 스택 트레이스 출력
			return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    

    //목표 complete 후 목표 달성 기록 테이블에 저장.
    @Operation(
    	    summary = "목표 완료 (Status:Complete)",
    	    description = "목표 complete 후 목표 달성 기록 테이블에 저장, 프론트측에서 목표조회에 있는 endDate로 조건을 설정해 endDate 이후에만 목표달성버튼 활성화 ",
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
    public ResponseEntity<?> updateGoalStatusToComplete(@PathVariable("goalId") Long goalId,@RequestParam("userId") Long userId) {
    	try {
        // Long 타입의 userId로 사용자 찾기
        AuthUser authUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new IllegalArgumentException("목표를 찾을 수 없습니다."));
        // 목표 상태 COMPLETE로 변경 (목표 횟수 달성 시)
        // userId를 authUser에서 가져와 전달
        Goal updatedGoal = goalService.updateGoalStatusToComplete(goalId, userId);
        goalRepository.save(updatedGoal);
        return new ResponseEntity<>(new CompleteResponseDto("목표 달성 완료"), HttpStatus.OK);
    	}catch (IllegalStateException e) {
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new CompleteResponseDto(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    




}
