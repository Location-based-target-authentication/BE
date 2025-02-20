package com.swyp.point.controller;


import com.swyp.point.dto.PointAddRequest;
import com.swyp.point.dto.PointBalanceResponse;
import com.swyp.point.dto.PointDedeductRequest;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;
    private final UserRepository userRepository;

    //포인트 조회
    @Operation(summary = "포인트 메인 페이지")
    @GetMapping("/{user_id}")
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
    @PostMapping("/{social_id}/add")
    public ResponseEntity<String> addPoints(@PathVariable("social_id") String socialId, @RequestBody PointAddRequest request){
        AuthUser authUser = findAuthUser(socialId);
        pointService.addPoints(authUser, request.getPoints(), request.getPointType(), request.getDescription(), request.getGoalId());
        return ResponseEntity.ok("포인트가 적립됨");
    }
    //포인트 차감
    @PostMapping("/{social_id}/deduct")
    public ResponseEntity<String> deductPoints(@PathVariable("social_id") String socialId, @RequestBody PointDedeductRequest request){
        AuthUser authUser = findAuthUser(socialId);
        boolean success = pointService.deductPoints(authUser, request.getPoints(), request.getPointType(), request.getDescription(), request.getGoalId());
        if(!success){return ResponseEntity.badRequest().body("포인트가 부족함");}
        return ResponseEntity.ok("포인트가 차감됨");
    }
    private AuthUser findAuthUser(String socialId){
        return userRepository.findBySocialId(socialId).orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

}
