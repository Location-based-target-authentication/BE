package com.swyp.global.config;

import com.swyp.social_login.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 자동으로 실행되는 작업을 수행하는 클래스
 */
@Component
@RequiredArgsConstructor
public class ApplicationStartupRunner implements ApplicationRunner {

    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 애플리케이션 시작 시 사용자 엔터티 연결 실행
        System.out.println("애플리케이션 시작: 사용자 엔터티 연결 작업 실행...");
        userService.linkUserEntities();
    }
} 