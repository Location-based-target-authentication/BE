package com.swyp.Swagger.controller;

import com.swyp.Swagger.dto.point.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "포인트", description = "포인트 관련 API")
@RestController
public class PointSwaggerController {

    @Operation(summary = "포인트 메인 페이지")
    @GetMapping("/api/v1/points/{user_id}")
    public PointMainResponse getPoints(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable("user_id") Long userId
    ) {
        return null;
    }

    @Operation(summary = "포인트 잔액 확인")
    @GetMapping("/api/v1/points/{user_id}/check")
    public PointBalanceResponse getPointBalance(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable("user_id") Long userId
    ) {
        return null;
    }

    @Operation(summary = "포인트 이력 조회")
    @GetMapping("/api/v1/points/{user_id}/history")
    public PointHistoryPageResponse getPointHistory(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable("user_id") Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return null;
    }

    @Operation(summary = "목표 확정 포인트 차감")
    @PostMapping("/api/v1/points/{user_id}/minus/goals")
    public PointHistoryResponse minusPointForGoal(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable("user_id") Long userId,
        @RequestBody PointDeductRequest request
    ) {
        return null;
    }

    @Operation(summary = "기프트 구매 포인트 차감")
    @PostMapping("/api/v1/points/{user_id}/minus/gift")
    public PointHistoryResponse minusPointForGift(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable("user_id") Long userId,
        @RequestBody GiftPurchaseRequest request
    ) {
        return null;
    }

    @Operation(summary = "웰컴 포인트 적립")
    @PostMapping("/api/v1/points/{user_id}/add/welcome")
    public PointHistoryResponse addWelcomePoint(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable("user_id") Long userId
    ) {
        return null;
    }

    @Operation(summary = "목표 달성 포인트 적립")
    @PostMapping("/api/v1/points/{user_id}/add/goalachieved")
    public PointHistoryResponse addGoalAchievedPoint(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable("user_id") Long userId,
        @RequestBody GoalAchievedRequest request
    ) {
        return null;
    }

    @Operation(summary = "보너스 포인트 적립")
    @PostMapping("/api/v1/points/{user_id}/add/bonus")
    public PointHistoryResponse addBonusPoint(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable("user_id") Long userId,
        @RequestBody BonusPointRequest request
    ) {
        return null;
    }
}