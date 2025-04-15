package com.swyp.point.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

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
    
    /**
     * HTML 형식의 이메일 발송 메서드
     * 
     * @param recipientEmail 수신자 이메일
     * @param subject 이메일 제목
     * @param content HTML 형식의 이메일 내용
     */
    public void sendEmail(String recipientEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(content, true); // true는 HTML 형식 활성화
            
            mailSender.send(message);
            System.out.println("HTML 이메일 전송 완료: " + recipientEmail);
        } catch (MessagingException e) {
            System.out.println("HTML 이메일 전송 처리 중 오류 발생 - 계속 진행합니다: " + e.getMessage());
            // 오류는 로그만 남기고 예외를 던지지 않음
        } catch (Exception e) {
            System.out.println("HTML 이메일 전송 처리 중 일반 오류 발생 - 계속 진행합니다: " + e.getMessage());
            // 오류는 로그만 남기고 예외를 던지지 않음
        }
    }
    
    /**
     * 쿠폰 발송 이메일
     * 
     * @param recipientEmail 수신자 이메일
     * @param recipientName 수신자 이름
     * @param couponType 쿠폰 유형 (스타벅스, 편의점 등)
     * @param couponCode 쿠폰 코드
     * @param points 사용된 포인트
     */
    public void sendCouponEmail(String recipientEmail, String recipientName, 
                               String couponType, String couponCode, int points) {
        try {
            String subject = "윌고: " + couponType + " 쿠폰이 발급되었습니다";
            
            StringBuilder content = new StringBuilder();
            content.append("<html><body style='font-family: Arial, sans-serif;'>");
            content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>");
            content.append("<h2 style='color: #4CAF50;'>안녕하세요, ").append(recipientName).append("님!</h2>");
            content.append("<p>포인트 교환으로 다음과 같은 쿠폰이 발행되었습니다:</p>");
            
            // 쿠폰 정보 박스
            content.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 15px 0;'>");
            content.append("<h3 style='color: #28a745;'>").append(couponType).append("</h3>");
            content.append("<p><strong>사용 포인트:</strong> ").append(points).append(" 포인트</p>");
            content.append("<p><strong>쿠폰 코드:</strong> <span style='background-color: #e9ecef; padding: 5px; border-radius: 3px; font-family: monospace;'>")
                   .append(couponCode).append("</span></p>");
            content.append("</div>");
            
            // 사용 안내
            content.append("<p>쿠폰 사용 방법:</p>");
            content.append("<ol>");
            if (couponType.contains("스타벅스")) {
                content.append("<li>스타벅스 매장 방문 또는 앱에서 주문 시 제시</li>");
                content.append("<li>바리스타에게 위 쿠폰 코드 제시</li>");
            } else {
                content.append("<li>해당 매장 방문 시 쿠폰 코드 제시</li>");
            }
            content.append("<li>쿠폰은 발급일로부터 30일간 유효합니다</li>");
            content.append("</ol>");
            
            content.append("<p style='margin-top: 20px;'>WillGo 이용해 주셔서 감사합니다!</p>");
            content.append("</div></body></html>");
            
            sendEmail(recipientEmail, subject, content.toString());
            System.out.println("쿠폰 이메일 생성 및 전송 요청 완료");
        } catch (Exception e) {
            System.out.println("쿠폰 이메일 생성 중 오류 발생 - 계속 진행합니다: " + e.getMessage());
            // 오류는 로그만 남기고 예외를 던지지 않음
        }
    }
}

