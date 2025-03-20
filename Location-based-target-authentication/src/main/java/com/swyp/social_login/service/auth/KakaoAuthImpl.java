package com.swyp.social_login.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class KakaoAuthImpl implements KakaoAuthService {
    private final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    
    // 재시도 설정
    private final int MAX_RETRY_COUNT = 3;
    private final long INITIAL_BACKOFF_MS = 1000; // 1초
    
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String KAKAO_CLIENT_SECRET;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URL;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri-local}")
    private String KAKAO_REDIRECT_URL_LOCAL;

    private final HttpServletRequest request;
    private static final Logger log = LoggerFactory.getLogger(KakaoAuthImpl.class);

    public KakaoAuthImpl(HttpServletRequest request) {
        this.request = request;
    }

    // 1. OAuth2 Access Token 발급
    @Override
    public String getAccessToken(String code) {
        log.info("카카오 인증 코드: {}", code);
        log.info("사용하는 리다이렉트 URI: {}", KAKAO_REDIRECT_URL);
        
        for (int retryCount = 0; retryCount <= MAX_RETRY_COUNT; retryCount++) {
            try {
                if (retryCount > 0) {
                    long backoffTime = INITIAL_BACKOFF_MS * (long) Math.pow(2, retryCount - 1);
                    log.info("[KakaoAuth] 재시도 #{} - {}ms 대기 후 시도합니다.", retryCount, backoffTime);
                    TimeUnit.MILLISECONDS.sleep(backoffTime);
                }
                
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                String referer = request.getHeader("Referer");
                String redirectUrl = (referer != null && referer.contains("localhost")) 
                    ? KAKAO_REDIRECT_URL_LOCAL 
                    : KAKAO_REDIRECT_URL;
                System.out.println("[KakaoAuth] Redirect URL 설정: " + redirectUrl);

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("grant_type", "authorization_code");
                params.add("client_id", KAKAO_CLIENT_ID);
                params.add("client_secret", KAKAO_CLIENT_SECRET);
                params.add("redirect_uri", redirectUrl);
                params.add("code", code);

                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
                System.out.println("[KakaoAuth] Access Token 요청 시작");

                ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, requestEntity, String.class);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String accessToken = jsonNode.get("access_token").asText();
                System.out.println("[KakaoAuth] Access Token 발급 성공");
                return accessToken;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) { // Too Many Requests
                    if (retryCount == MAX_RETRY_COUNT) {
                        log.error("[KakaoAuth] 최대 재시도 횟수({})를 초과했습니다. 요청 속도 제한(429)으로 실패", MAX_RETRY_COUNT);
                        throw new RuntimeException("카카오 API 요청 속도 제한 초과", e);
                    }
                    log.warn("[KakaoAuth] 요청 속도 제한(429) 발생, 재시도 #{}", retryCount + 1);
                } else {
                    log.error("[KakaoAuth] Access Token 발급 실패: HTTP 오류 {}", e.getStatusCode());
                    throw new RuntimeException("카카오 Access Token 요청 실패: " + e.getMessage(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[KakaoAuth] 재시도 대기 중 인터럽트 발생");
                throw new RuntimeException("카카오 Access Token 요청 중단", e);
            } catch (Exception e) {
                log.error("[KakaoAuth] Access Token 발급 실패: {}", e.getMessage());
                throw new RuntimeException("카카오 Access Token 요청 실패", e);
            }
        }
        throw new RuntimeException("카카오 Access Token 요청 실패 - 재시도 후에도 실패");
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        System.out.println("[KakaoAuth] 사용자 정보 조회 시작");
        
        for (int retryCount = 0; retryCount <= MAX_RETRY_COUNT; retryCount++) {
            try {
                if (retryCount > 0) {
                    long backoffTime = INITIAL_BACKOFF_MS * (long) Math.pow(2, retryCount - 1);
                    log.info("[KakaoAuth] 사용자 정보 조회 재시도 #{} - {}ms 대기 후 시도합니다.", retryCount, backoffTime);
                    TimeUnit.MILLISECONDS.sleep(backoffTime);
                }
                
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(KAKAO_USER_INFO_URL, HttpMethod.GET, entity, String.class);
                System.out.println("[KakaoAuth] 카카오 API 응답 수신 성공");

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                Map<String, Object> userInfo = new HashMap<>();
                String socialId = jsonNode.get("id").asText();
                String email = jsonNode.get("kakao_account").get("email").asText();
                String username = jsonNode.get("kakao_account").get("profile").get("nickname").asText();

                userInfo.put("socialId", socialId);
                userInfo.put("email", email);
                userInfo.put("username", username);

                System.out.println("[KakaoAuth] 사용자 정보 파싱 완료");
                log.debug("[KakaoAuth] socialId: {}, email: {}, username: {}", socialId, email, username);

                return userInfo;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) { // Too Many Requests
                    if (retryCount == MAX_RETRY_COUNT) {
                        log.error("[KakaoAuth] 사용자 정보 조회 - 최대 재시도 횟수({})를 초과했습니다. 요청 속도 제한(429)으로 실패", MAX_RETRY_COUNT);
                        throw new RuntimeException("카카오 API 요청 속도 제한 초과", e);
                    }
                    log.warn("[KakaoAuth] 사용자 정보 조회 - 요청 속도 제한(429) 발생, 재시도 #{}", retryCount + 1);
                } else {
                    log.error("[KakaoAuth] 사용자 정보 조회 실패: HTTP 오류 {}", e.getStatusCode());
                    throw new RuntimeException("카카오 사용자 정보 요청 실패: " + e.getMessage(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[KakaoAuth] 사용자 정보 조회 - 재시도 대기 중 인터럽트 발생");
                throw new RuntimeException("카카오 사용자 정보 요청 중단", e);
            } catch (Exception e) {
                log.error("[KakaoAuth] 사용자 정보 조회 실패: {}", e.getMessage());
                throw new RuntimeException("카카오 사용자 정보 요청 실패", e);
            }
        }
        throw new RuntimeException("카카오 사용자 정보 요청 실패 - 재시도 후에도 실패");
    }
}


