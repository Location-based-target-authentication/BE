package com.swyp.users.controller;

import com.swyp.users.domain.User;
import com.swyp.users.service.UserManagementService;
import com.swyp.users.dto.UserModifyRequest;
import com.swyp.users.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 관리", description = "사용자 관리 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @Operation(summary = "회원 로그아웃", description = "사용자를 로그아웃 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/{userId}/logout")
    public ResponseEntity<Void> logout(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        userService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자 정보 조회", description = "사용자의 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/check")
    public ResponseEntity<UserResponse> getUserInfo(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        return ResponseEntity.ok(new UserResponse(userService.getUserInfo(userId)));
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자의 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PatchMapping("/{userId}/modify")
    public ResponseEntity<User> modifyUserInfo(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId,
        @RequestBody UserModifyRequest request
    ) {
        return ResponseEntity.ok(userService.modifyUserInfo(userId, request));
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
} 