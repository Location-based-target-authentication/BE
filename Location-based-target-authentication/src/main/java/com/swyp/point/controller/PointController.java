package com.swyp.point.controller;
import com.swyp.point.dto.PointAddRequest;
import com.swyp.point.dto.PointBalanceResponse;
import com.swyp.point.dto.PointDedeductRequest;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;
    private final UserRepository userRepository;

    //포인트 조회
    @Operation(
            summary = "포인트 메인 페이지",
            description = "사용자의 현재 포인트 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "포인트 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"userId\": 1, \"socialId\": \"social_123\", \"totalPoints\": 500}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"error\": \"사용자를 찾을 수 없습니다.\"}")
                            )
                    )
            }
    )
    @GetMapping("/{social_id}")
    public ResponseEntity<PointBalanceResponse> getPoints( @PathVariable("social_id") String socialId){
        AuthUser authUser = findAuthUser(socialId);
        int points = pointService.getUserPoints(authUser);
        PointBalanceResponse response = new PointBalanceResponse(
                authUser.getId(),
                authUser.getSocialId(),
                points
        );
        response.setTotalPoints(points);
        return ResponseEntity.ok(response);
    }
    //포인트 적립
    @Operation(
            summary = "포인트 적립",
            description = "사용자의 포인트를 적립합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "포인트 적립 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"message\": \"포인트가 적립됨\"}")
                            )

                    )
            }
    )
    @PostMapping("/{social_id}/add")
    public ResponseEntity<String> addPoints(@PathVariable("social_id") String socialId, @RequestBody PointAddRequest request){
        AuthUser authUser = findAuthUser(socialId);
        pointService.addPoints(authUser, request.getPoints(), request.getPointType(), request.getDescription(), request.getGoalId());
        return ResponseEntity.ok("포인트가 적립됨");
    }
    //포인트 차감
    @Operation(
            summary = "포인트 차감",
            description = "사용자의 포인트를 차감합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "포인트 차감 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"status\": \"success\", \"message\": \"포인트가 차감됨\", \"totalPoints\": 450}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "포인트가 부족함",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"status\": \"fail\", \"message\": \"포인트가 부족함\"}")
                            )
                    )
            }
    )
    @PostMapping("/{social_id}/deduct")
    public ResponseEntity<Map<String, Object>> deductPoints(@PathVariable("social_id") String socialId, @RequestBody PointDedeductRequest request) {
        AuthUser authUser = findAuthUser(socialId);
        boolean success = pointService.deductPoints(authUser, request.getPoints(), request.getPointType(), request.getDescription(), request.getGoalId());
        Map<String, Object> response = new HashMap<>();
        if (!success) {
            response.put("status", "fail");
            response.put("message", "포인트가 부족함");
            return ResponseEntity.badRequest().body(response);
        }
        int updatedPoints = pointService.getUserPoints(authUser);
        response.put("status", "success");
        response.put("message", "포인트가 차감됨");
        response.put("totalPoints", updatedPoints);
        return ResponseEntity.ok(response);
    }
    private AuthUser findAuthUser(String socialId){
        return userRepository.findBySocialId(socialId).orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }


}
