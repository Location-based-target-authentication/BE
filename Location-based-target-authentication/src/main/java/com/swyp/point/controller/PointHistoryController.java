package com.swyp.point.controller;
import com.swyp.point.dto.PointHistoryResponse;
import com.swyp.point.entity.PointHistory;
import com.swyp.point.repository.PointHistoryRepository;
import com.swyp.point.service.PointService;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/points/history")
@RequiredArgsConstructor
public class PointHistoryController {

    private final PointService pointService;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Operation(summary = "포인트 이력 조회")
    @GetMapping("/{user_id}")
    public ResponseEntity<PointHistoryResponse> getPointHistory(@PathVariable("user_id") Long userId) {
        AuthUser authUser = findAuthUser(userId);
        List<PointHistory> historyList = pointService.getPointHistory(authUser.getId());
        PointHistoryResponse response = new PointHistoryResponse(
                authUser.getId(),
                authUser.getUsername(),
                historyList
        );
        return ResponseEntity.ok(response);
    }

    private AuthUser findAuthUser(Long id) {
        System.out.println("요청한 userId: " + id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    System.out.println("사용자를 찾을 수 없음.. " + id);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });
    }
}


