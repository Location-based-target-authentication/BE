package com.swyp.users.controller;

import com.swyp.users.domain.Terms;
import com.swyp.users.service.TermsService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Tag(name = "서비스 이용약관", description = "서비스 이용약관 동의 관련 API")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "서비스 이용약관 동의 상태 조회", description = "사용자의 서비스 이용약관 동의 여부를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<Terms> getTerms(@PathVariable Long userId) {
        return ResponseEntity.ok(termsService.getTerms(userId));
    }

    @Operation(summary = "서비스 이용약관 동의", description = "사용자가 서비스 이용약관에 동의합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "동의 처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/agree/{userId}")
    public ResponseEntity<Void> agreeToTerms(@PathVariable Long userId) {
        termsService.agreeToTerms(userId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
} 