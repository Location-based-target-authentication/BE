package com.swyp.Swagger.controller;

import com.swyp.Swagger.dto.user.UserResponse;
import com.swyp.Swagger.dto.user.UserModifyRequest;
import com.swyp.Swagger.dto.user.AutoLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "회원 관련 API")
@RestController
public class UserSwaggerController {
    
    @Operation(
        summary = "로그아웃",
        description = "회원 로그아웃을 진행합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "로그아웃 성공"
            )
        }
    )
    @PostMapping("/api/v1/users/{user_id}/logout")
    public void logout(@PathVariable("user_id") Long userId) {
    }

    @Operation(
        summary = "사용자 정보 조회",
        description = "사용자 정보를 조회합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            )
        }
    )
    @GetMapping("/api/v1/users/{user_id}/check")
    public UserResponse getUserInfo(@PathVariable("user_id") Long userId) {
        return null;
    }

    @Operation(
        summary = "사용자 정보 수정",
        description = "사용자 정보를 수정합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            )
        }
    )
    @PatchMapping("/api/v1/users/{user_id}/modify")
    public UserResponse modifyUserInfo(
        @PathVariable("user_id") Long userId,
        @RequestBody UserModifyRequest request
    ) {
        return null;
    }

    @Operation(
        summary = "자동 로그인 설정",
        description = "자동 로그인을 설정합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "설정 성공"
            )
        }
    )
    @PatchMapping("/api/v1/users/autoLogin")
    public void setAutoLogin(@RequestBody AutoLoginRequest request) {
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "회원 탈퇴를 진행합니다.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "탈퇴 성공"
            )
        }
    )
    @DeleteMapping("/api/v1/users/{user_id}/delete")
    public void deleteUser(@PathVariable("user_id") Long userId) {
    }
} 