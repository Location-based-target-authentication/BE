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
import com.swyp.global.cache.AuthRequestCacheService;

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
    private final AuthRequestCacheService cacheService;
    private static final Logger log = LoggerFactory.getLogger(KakaoAuthImpl.class);

    public KakaoAuthImpl(HttpServletRequest request, AuthRequestCacheService cacheService) {
        this.request = request;
        this.cacheService = cacheService;
    }

    // 1. OAuth2 Access Token 발급
    @Override
    public String getAccessToken(String code) {
        log.info("카카오 인증 코드: {}", code);
        log.info("사용하는 리다이렉트 URI: {}", KAKAO_REDIRECT_URL);
        
        // 캐시에서 코드에 해당하는 액세스 토큰 찾기
        String cachedToken = cacheService.getAccessTokenByCode(code);
        if (cachedToken != null) {
            log.info("캐시에서 액세스 토큰을 찾았습니다. 새로운 API 요청을 하지 않습니다.");
            return cachedToken;
        }
        
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
                String redirectUrl;
                
                // localhost 개발 환경에서만 로컬 리다이렉트 URL 사용, 그 외에는 기본 리다이렉트 URL 사용
                if (referer != null && referer.contains("localhost")) {
                    redirectUrl = KAKAO_REDIRECT_URL_LOCAL;
                    log.info("[KakaoAuth] 로컬 환경 감지, 로컬 리다이렉트 URL 사용: {}", KAKAO_REDIRECT_URL_LOCAL);
                } else {
                    redirectUrl = KAKAO_REDIRECT_URL;
                    log.info("[KakaoAuth] 프로덕션 환경, 기본 리다이렉트 URL 사용: {}", KAKAO_REDIRECT_URL);
                }

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("grant_type", "authorization_code");
                params.add("client_id", KAKAO_CLIENT_ID);
                params.add("client_secret", KAKAO_CLIENT_SECRET);
                params.add("redirect_uri", redirectUrl);
                params.add("code", code);

                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
                System.out.println("[KakaoAuth] Access Token 요청 시작");

                ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, requestEntity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    String accessToken = jsonNode.get("access_token").asText();
                    
                    // 액세스 토큰을 캐시에 저장
                    cacheService.cacheAccessTokenByCode(code, accessToken);
                    
                    return accessToken;
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.warn("[KakaoAuth] API 요청 한도 초과, 재시도 {}/{}", retryCount, MAX_RETRY_COUNT);
                    if (retryCount == MAX_RETRY_COUNT) {
                        log.error("[KakaoAuth] 최대 재시도 횟수 초과: {}", e.getMessage());
                        throw new RuntimeException("카카오 API 요청 한도를 초과했습니다.", e);
                    }
                    // 다음 재시도를 위해 계속
                    continue;
                } else {
                    log.error("[KakaoAuth] HTTP 에러: {}", e.getMessage());
                    throw new RuntimeException("카카오 API 통신 중 오류가 발생했습니다: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                log.error("[KakaoAuth] 일반 에러: {}", e.getMessage());
                throw new RuntimeException("카카오 API 통신 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }
        
        throw new RuntimeException("카카오 액세스 토큰을 가져오는데 실패했습니다.");
    }

    // 2. 사용자 정보 가져오기
    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(KAKAO_USER_INFO_URL, HttpMethod.GET, entity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            // 사용자 식별자 (이후 캐싱을 위해 사용)
            String socialId = rootNode.path("id").asText();
            
            // 프로필 정보
            JsonNode properties = rootNode.path("properties");
            String username = properties.path("nickname").asText();
            String profileImage = properties.path("profile_image").asText();
            
            // 이메일 정보
            String email = "";
            if (!rootNode.path("kakao_account").path("email").isMissingNode()) {
                email = rootNode.path("kakao_account").path("email").asText();
            }
            
            // 인증 정보에 성공하면 사용자 ID로 토큰을 캐싱
            if (!socialId.isEmpty()) {
                cacheService.cacheAccessTokenByUserId(socialId, accessToken);
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("socialId", socialId);
            userInfo.put("username", username);
            userInfo.put("email", email);
            userInfo.put("profileImage", profileImage);
            
            return userInfo;
        } catch (Exception e) {
            log.error("[KakaoAuth] 사용자 정보 요청 중 오류: {}", e.getMessage());
            throw new RuntimeException("카카오 API에서 사용자 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}


