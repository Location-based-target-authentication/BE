package com.swyp.login;

import com.swyp.global.security.JwtAuthenticationFilter;
import com.swyp.global.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class JwtUtilFilterTest {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 1. 의존성 주입 테스트: JwtUtil과 JwtAuthenticationFilter 빈이 정상적으로 주입되었는지
    @Test
    public void testDependencyInjection() {
        System.out.println("[TEST] JwtUtil bean: " + jwtUtil);
        System.out.println("[TEST] JwtAuthenticationFilter bean: " + jwtAuthenticationFilter);
        assertNotNull(jwtUtil, "JwtUtil bean은 null이 아님");
        assertNotNull(jwtAuthenticationFilter, "JwtAuthenticationFilter bean은 null이 아님");
    }

    // 2. Authorization 헤더에서 토큰 추출 테스트: resolveToken() 메서드를 직접 호출하여 확인
    @Test
    public void testResolveTokenExtraction() {
        // 요청에 Authorization 헤더를 포함하여 생성
        MockHttpServletRequest request = new MockHttpServletRequest();
        String sampleToken = "sampleToken123";
        request.addHeader("Authorization", "Bearer " + sampleToken);

        // private 메서드 resolveToken()를 ReflectionTestUtils를 통해 호출
        String extractedToken = (String) ReflectionTestUtils.invokeMethod(
                jwtAuthenticationFilter, "resolveToken", request);

        System.out.println("[TEST] Extracted Token: " + extractedToken);
        assertEquals(sampleToken, extractedToken, "추출된 토큰은 입력한 토큰과 동일해야 합니다.");

        // Authorization 헤더가 없을 때의 케이스도 확인
        MockHttpServletRequest requestNoHeader = new MockHttpServletRequest();
        String extractedToken2 = (String) ReflectionTestUtils.invokeMethod(
                jwtAuthenticationFilter, "resolveToken", requestNoHeader);
        assertNull(extractedToken2, "헤더가 없으면 null이 반환되어야 합니다.");
    }
}