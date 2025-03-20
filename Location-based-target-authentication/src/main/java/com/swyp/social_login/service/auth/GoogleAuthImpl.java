package com.swyp.social_login.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GoogleAuthImpl implements GoogleAuthService {
    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    // 재시도 설정
    private final int MAX_RETRY_COUNT = 3;
    private final long INITIAL_BACKOFF_MS = 1000; // 1초
    
    private static final Logger log = LoggerFactory.getLogger(GoogleAuthImpl.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUrl;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri-local}")
    private String redirectUrlLocal;

    private final HttpServletRequest request;

    public GoogleAuthImpl(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getAccessToken(String code) {
        log.info("[GoogleAuthImpl] getAccessToken 시작 - code: {}", code);
        
        for (int retryCount = 0; retryCount <= MAX_RETRY_COUNT; retryCount++) {
            try {
                if (retryCount > 0) {
                    long backoffTime = INITIAL_BACKOFF_MS * (long) Math.pow(2, retryCount - 1);
                    log.info("[GoogleAuthImpl] 재시도 #{} - {}ms 대기 후 시도합니다.", retryCount, backoffTime);
                    TimeUnit.MILLISECONDS.sleep(backoffTime);
                }
                
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                String finalRedirectUrl = redirectUrl;
                log.info("[GoogleAuthImpl] 선택된 redirectUrl: {}", finalRedirectUrl);

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("grant_type", "authorization_code");
                params.add("client_id", clientId);
                params.add("client_secret", clientSecret);
                params.add("redirect_uri", finalRedirectUrl);
                params.add("code", code);

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
                ResponseEntity<String> response = restTemplate.exchange(GOOGLE_TOKEN_URL, HttpMethod.POST, request, String.class);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String accessToken = jsonNode.get("access_token").asText();
                log.info("[GoogleAuthImpl] Access Token 발급 성공");
                return accessToken;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) { // Too Many Requests
                    if (retryCount == MAX_RETRY_COUNT) {
                        log.error("[GoogleAuthImpl] 최대 재시도 횟수({})를 초과했습니다. 요청 속도 제한(429)으로 실패", MAX_RETRY_COUNT);
                        throw new RuntimeException("구글 API 요청 속도 제한 초과", e);
                    }
                    log.warn("[GoogleAuthImpl] 요청 속도 제한(429) 발생, 재시도 #{}", retryCount + 1);
                } else if (e.getStatusCode().value() == 400) {
                    log.error("[GoogleAuthImpl] 잘못된 요청 (400): {}", e.getResponseBodyAsString());
                    throw new RuntimeException("구글 Access Token 요청 실패: 잘못된 요청 - " + e.getResponseBodyAsString(), e);
                } else {
                    log.error("[GoogleAuthImpl] Access Token 발급 실패: HTTP 오류 {}", e.getStatusCode());
                    throw new RuntimeException("구글 Access Token 요청 실패: " + e.getMessage(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[GoogleAuthImpl] 재시도 대기 중 인터럽트 발생");
                throw new RuntimeException("구글 Access Token 요청 중단", e);
            } catch (Exception e) {
                log.error("[GoogleAuthImpl] Access Token 발급 실패: {}", e.getMessage());
                throw new RuntimeException("구글 Access Token 요청 실패", e);
            }
        }
        throw new RuntimeException("구글 Access Token 요청 실패 - 재시도 후에도 실패");
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        log.info("[GoogleAuthImpl] getUserInfo 시작");
        
        for (int retryCount = 0; retryCount <= MAX_RETRY_COUNT; retryCount++) {
            try {
                if (retryCount > 0) {
                    long backoffTime = INITIAL_BACKOFF_MS * (long) Math.pow(2, retryCount - 1);
                    log.info("[GoogleAuthImpl] 사용자 정보 조회 재시도 #{} - {}ms 대기 후 시도합니다.", retryCount, backoffTime);
                    TimeUnit.MILLISECONDS.sleep(backoffTime);
                }
                
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_URL, HttpMethod.GET, entity, String.class);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                Map<String, Object> userInfo = new HashMap<>();
                String socialId = jsonNode.get("id").asText();
                String email = jsonNode.get("email").asText();
                String username = jsonNode.get("name").asText();
                
                log.info("[GoogleAuthImpl] 사용자 정보 파싱 결과: socialId={}, email={}, username={}", 
                        socialId, email, username);
                
                userInfo.put("socialId", socialId);
                userInfo.put("email", email);
                userInfo.put("username", username);

                log.info("[GoogleAuthImpl] 사용자 정보 조회 성공");
                return userInfo;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) { // Too Many Requests
                    if (retryCount == MAX_RETRY_COUNT) {
                        log.error("[GoogleAuthImpl] 사용자 정보 조회 - 최대 재시도 횟수({})를 초과했습니다. 요청 속도 제한(429)으로 실패", MAX_RETRY_COUNT);
                        throw new RuntimeException("구글 API 요청 속도 제한 초과", e);
                    }
                    log.warn("[GoogleAuthImpl] 사용자 정보 조회 - 요청 속도 제한(429) 발생, 재시도 #{}", retryCount + 1);
                } else {
                    log.error("[GoogleAuthImpl] 사용자 정보 조회 실패: HTTP 오류 {}", e.getStatusCode());
                    throw new RuntimeException("구글 사용자 정보 요청 실패: " + e.getMessage(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[GoogleAuthImpl] 사용자 정보 조회 - 재시도 대기 중 인터럽트 발생");
                throw new RuntimeException("구글 사용자 정보 요청 중단", e);
            } catch (Exception e) {
                log.error("[GoogleAuthImpl] 사용자 정보 조회 실패: {}", e.getMessage());
                log.error("[GoogleAuthImpl] 응답 내용: {}", e.getMessage());
                throw new RuntimeException("구글 사용자 정보 요청 실패", e);
            }
        }
        throw new RuntimeException("구글 사용자 정보 요청 실패 - 재시도 후에도 실패");
    }
}
