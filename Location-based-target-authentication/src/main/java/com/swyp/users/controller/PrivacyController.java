package com.swyp.users.controller;

import com.swyp.users.service.UserAgreementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@Tag(name = "개인정보 처리방침", description = "개인정보 처리방침 API")
@RestController
@RequestMapping("/api/v1/privacy")
@RequiredArgsConstructor
public class PrivacyController {

    private final UserAgreementService userAgreementService;

    @Operation(summary = "개인정보 처리방침 조회", description = "사용자의 개인정보 처리방침 동의 상태를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<Boolean> getPrivacyAgreement(@PathVariable Long userId) {
        return ResponseEntity.ok(userAgreementService.getPrivacyAgreement(userId));
    }

    @Operation(summary = "개인정보 처리방침 동의", description = "사용자가 개인정보 처리방침에 동의합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동의 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/agree/{userId}")
    public ResponseEntity<Void> agreeToPrivacyPolicy(@PathVariable Long userId) {
        userAgreementService.agreeToPrivacyPolicy(userId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}