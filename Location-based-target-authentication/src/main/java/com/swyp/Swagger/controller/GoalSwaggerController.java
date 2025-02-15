package com.swyp.Swagger.controller;

import com.swyp.Swagger.dto.goal.GoalCreateRequest;
import com.swyp.Swagger.dto.goal.GoalResponse;
import com.swyp.Swagger.dto.goal.GoalUpdateRequest;
import com.swyp.Swagger.dto.goal.GoalAchievementResponse;
import com.swyp.Swagger.dto.goal.GoalListResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "목표", description = "목표 관련 API")
@RestController
public class GoalSwaggerController {

    @Operation(
        summary = "목표 메인 페이지",
        description = "목표 메인 페이지를 조회합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(
                    implementation = List.class,
                    subTypes = {GoalResponse.class}
                ))
            )
        }
    )
    @GetMapping("/api/v1/goals")
    public List<GoalResponse> getGoals() {
        return null;
    }

    @Operation(
        summary = "전체 목표 조회",
        description = "전체 목표 목록을 조회합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = GoalListResponse.class))
            )
        }
    )
    @GetMapping("/api/v1/goals/check")
    public GoalListResponse getGoalList() {
        return null;
    }

    @Operation(
        summary = "상세 목표 조회",
        description = "목표 상세 정보를 조회합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = GoalResponse.class))
            )
        }
    )
    @PostMapping("/api/v1/goals/check/{goalID}")
    public GoalResponse getGoalDetail(
        @Parameter(description = "목표 ID", example = "1") 
        @PathVariable Long goalID
    ) {
        return null;
    }

    @Operation(
        summary = "목표 생성",
        description = "새로운 목표를 생성합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = @Content(schema = @Schema(implementation = GoalResponse.class))
            )
        }
    )
    @PostMapping("/api/v1/goals/create")
    public GoalResponse createGoal(
        @RequestBody @Schema(description = "목표 생성 정보") GoalCreateRequest request
    ) {
        return null;
    }

    @Operation(
        summary = "임시 저장된 목표 수정",
        description = "임시 저장된 목표를 수정합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = @Content(schema = @Schema(implementation = GoalResponse.class))
            )
        }
    )
    @PatchMapping("/api/v1/goals/{goalID}/draft")
    public GoalResponse updateDraftGoal(
        @Parameter(description = "목표 ID", example = "1") 
        @PathVariable Long goalID,
        @RequestBody @Schema(description = "목표 수정 정보") GoalUpdateRequest request
    ) {
        return null;
    }

    @Operation(
        summary = "목표 활성화",
        description = "목표를 활성화합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "활성화 성공",
                content = @Content(schema = @Schema(implementation = GoalResponse.class))
            )
        }
    )
    @PostMapping("/api/v1/goals/{goalID}/activate")
    public GoalResponse activateGoal(
        @Parameter(description = "목표 ID", example = "1") 
        @PathVariable Long goalID
    ) {
        return null;
    }

    @Operation(
        summary = "목표 삭제",
        description = "목표를 삭제합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "삭제 성공"
            )
        }
    )
    @DeleteMapping("/api/v1/goals/{goalID}/delete")
    public void deleteGoal(
        @Parameter(description = "목표 ID", example = "1") 
        @PathVariable Long goalID
    ) {
    }

    @Operation(
        summary = "목표 달성 인증",
        description = "목표 달성 인증을 진행합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "인증 성공",
                content = @Content(schema = @Schema(implementation = GoalAchievementResponse.class))
            )
        }
    )
    @PostMapping("/api/v1/goals/{goalID}/complete")
    public GoalAchievementResponse achieveGoal(
        @Parameter(description = "목표 ID", example = "1") 
        @PathVariable Long goalID
    ) {
        return null;
    }
} 