package com.swyp.point.controller;

import com.swyp.point.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailTestController {

    private final MailService mailService;

    @GetMapping("/test")
    public ResponseEntity<String> testSendMail() {
        mailService.sendCouponEmail(
                "jangmj80@naver.com",
                "장민지",
                "테스트 기프티콘",
                "TEST-1234-ABCD",
                3000
        );
        return ResponseEntity.ok("이메일 발송 요청 완료!");
    }
}
