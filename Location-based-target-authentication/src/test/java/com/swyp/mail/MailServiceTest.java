package com.swyp.mail;
import com.swyp.point.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Test
    void testSendCouponEmail() {
        mailService.sendCouponEmail(
                "jangmj80@naver.com",
                "장민지",
                "테스트 기프티콘",
                "TEST-CODE-1234",
                5000
        );
    }
}


