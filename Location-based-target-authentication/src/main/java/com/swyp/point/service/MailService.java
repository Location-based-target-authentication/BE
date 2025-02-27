package com.swyp.point.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    public void sendGiftNotification(String recipientEmail, String username, String phonenum, String giftType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("쿠폰 사용 알림");
            message.setText("사용자 " + username + "님이 " + giftType + " 쿠폰을 사용했습니다. 사용자 번호: " + phonenum);
            mailSender.send(message);
            System.out.println("이메일 전송 완료: " + recipientEmail);
        } catch (Exception e) {
            System.err.println("이메일 전송 실패: " + e.getMessage());
        }
    }
}

