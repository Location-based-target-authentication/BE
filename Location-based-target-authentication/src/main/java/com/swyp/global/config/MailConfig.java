package com.swyp.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${MAIL_HOST:smtp.gmail.com}")
    private String host;

    @Value("${MAIL_PORT:587}")
    private int port;

    @Value("${MAIL_USERNAME:}")
    private String username;

    @Value("${MAIL_PASSWORD:}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.debug", "true");
        
        // 인증 실패 시 조용히 처리되도록 설정
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        props.put("mail.smtp.auth.plain.disable", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "false");
        
        // 이메일 전송 실패 시 재시도 횟수 설정 (재시도 안 함)
        mailSender.getJavaMailProperties().put("mail.smtp.sendpartial", "true");
        mailSender.getJavaMailProperties().put("mail.smtp.dsn.notify", "NEVER");
        mailSender.getJavaMailProperties().put("mail.smtp.dsn.ret", "FULL");

        return mailSender;
    }
}