package com.swyp.users.controller;

import com.swyp.users.domain.PrivateAgreement;
import com.swyp.users.service.PrivateAgreementService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "개인정보 처리방침", description = "개인정보 처리방침 동의 관련 API")
@RestController
@RequestMapping("/api/v1/privateagreement")
@RequiredArgsConstructor
public class PrivateAgreementController {

    private final PrivateAgreementService privateAgreementService;

    @Operation(summary = "개인정보 처리방침 동의 상태 조회", description = "사용자의 개인정보 처리방침 동의 여부를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<PrivateAgreement> getPrivateAgreement(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(privateAgreementService.getPrivateAgreement(userId));
    }

    @Operation(summary = "개인정보 처리방침 동의", description = "사용자가 개인정보 처리방침에 동의합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "동의 처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/agree")
    public ResponseEntity<Void> agreeToPrivateAgreement(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @RequestParam Long userId
    ) {
        privateAgreementService.agreeToPrivateAgreement(userId);
        return ResponseEntity.ok().build();
    }
} 