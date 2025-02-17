package com.swyp.Swagger.controller;

import com.swyp.Swagger.dto.terms.TermsAgreementRequest;
import com.swyp.Swagger.dto.terms.TermsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "약관", description = "약관 관련 API")
@RestController
public class TermsSwaggerController {

    @Operation(summary = "개인정보 처리")
    @GetMapping("/api/v1/privateagreement")
    public TermsResponse getPrivacyPolicy() {
        return null;
    }

    @Operation(summary = "개인정보 처리 동의")
    @PostMapping("/api/v1/privateagreement/agree")
    public void agreePrivacyPolicy(@RequestBody TermsAgreementRequest request) {
    }

    @Operation(summary = "서비스 약관")
    @GetMapping("/api/v1/terms")
    public TermsResponse getTerms() {
        return null;
    }

    @Operation(summary = "서비스 약관 동의")
    @PostMapping("/api/v1/terms/agree")
    public void agreeTerms(@RequestBody TermsAgreementRequest request) {
    }
}

