package com.swyp.point.controller;
import com.swyp.point.dto.PointAddRequest;
import com.swyp.point.dto.PointBalanceResponse;
import com.swyp.point.dto.PointDedeductRequest;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import com.swyp.global.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
    @GetMapping("/{userId}")
    public ResponseEntity<PointBalanceResponse> getPoints(
            @PathVariable("userId") Long pathUserId) {
        try {
            AuthUser authUser = findAuthUser(pathUserId);
            int points = pointService.getUserPoints(authUser);
            PointBalanceResponse response = new PointBalanceResponse(
                    authUser.getId(),
                    authUser.getUsername(),
                    points
            );
            response.setTotalPoints(points);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다");
        }
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
    @PostMapping("/{userId}/add")
    public ResponseEntity<Map<String, Object>> addPoints(
            @PathVariable("userId") Long pathUserId,
            @RequestBody PointAddRequest pointRequest) {
        try {
            AuthUser authUser = findAuthUser(pathUserId);
            pointService.addPoints(authUser, pointRequest.getPoints(), pointRequest.getPointType(), pointRequest.getDescription(), pointRequest.getGoalId());
            int updatedPoints = pointService.getUserPoints(authUser);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "포인트가 적립됨");
            response.put("totalPoints", updatedPoints);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "사용자를 찾을 수 없습니다");
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
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
    @PostMapping("/{userId}/deduct")
    public ResponseEntity<Map<String, Object>> deductPoints(
            @PathVariable("userId") Long pathUserId,
            @RequestBody PointDedeductRequest pointRequest,
            HttpServletRequest request) {
        try {
            // userId로 사용자 찾기
            AuthUser authUser = findAuthUser(pathUserId);
            
            try {
                // 포인트 차감 처리
                boolean success = pointService.deductPoints(authUser, pointRequest.getPoints(), pointRequest.getPointType(), pointRequest.getDescription(), pointRequest.getGoalId());
                if (success) {
                    int updatedPoints = pointService.getUserPoints(authUser);
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "포인트가 성공적으로 차감되었습니다");
                    response.put("totalPoints", updatedPoints);
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "fail");
                    response.put("message", "포인트 차감에 실패했습니다");
                    return ResponseEntity.badRequest().body(response);
                }
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "fail");
                response.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "사용자를 찾을 수 없습니다");
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private AuthUser findAuthUser(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }


}
