package com.swyp.Swagger.controller;

import com.swyp.Swagger.dto.point.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "포인트", description = "포인트 관련 API")
@RestController
public class PointSwaggerController {

    @Operation(summary = "포인트 메인 페이지")
    @GetMapping("/api/v1/points")
    public PointMainResponse getPoints() {
        return null;
    }

    @Operation(summary = "포인트 잔액 확인")
    @GetMapping("/api/v1/points/check")
    public PointBalanceResponse getPointBalance() {
        return null;
    }

    @Operation(summary = "포인트 이력 조회")
    @GetMapping("/api/v1/points/history")
    public PointHistoryPageResponse getPointHistory(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return null;
    }

    @Operation(summary = "목표 확정 포인트 차감")
    @PostMapping("/api/v1/points/minus/goals")
    public PointHistoryResponse minusPointForGoal(
        @RequestBody PointDeductRequest request
    ) {
        return null;
    }

    @Operation(summary = "기프트 구매 포인트 차감")
    @PostMapping("/api/v1/points/minus/gift")
    public PointHistoryResponse minusPointForGift(
        @RequestBody GiftPurchaseRequest request
    ) {
        return null;
    }

    @Operation(summary = "웰컴 포인트 적립")
    @PostMapping("/api/v1/points/add/welcome")
    public PointHistoryResponse addWelcomePoint() {
        return null;
    }

    @Operation(summary = "목표 달성 포인트 적립")
    @PostMapping("/api/v1/points/add/goalachieved")
    public PointHistoryResponse addGoalAchievedPoint(
        @RequestBody GoalAchievedRequest request
    ) {
        return null;
    }

    @Operation(summary = "보너스 포인트 적립")
    @PostMapping("/api/v1/points/add/bonus")
    public PointHistoryResponse addBonusPoint(
        @RequestBody BonusPointRequest request
    ) {
        return null;
    }
}