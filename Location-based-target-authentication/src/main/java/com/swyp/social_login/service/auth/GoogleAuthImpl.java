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
import com.swyp.global.cache.AuthRequestCacheService;

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
    private final AuthRequestCacheService cacheService;

    public GoogleAuthImpl(HttpServletRequest request, AuthRequestCacheService cacheService) {
        this.request = request;
        this.cacheService = cacheService;
    }

    @Override
    public String getAccessToken(String code) {
        log.info("구글 인증 코드: {}", code);
        
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
                    log.info("[GoogleAuth] 재시도 #{} - {}ms 대기 후 시도합니다.", retryCount, backoffTime);
                    TimeUnit.MILLISECONDS.sleep(backoffTime);
                }
                
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                
                String referer = request.getHeader("Referer");
                String finalRedirectUrl = (referer != null && referer.contains("localhost")) 
                    ? redirectUrlLocal 
                    : redirectUrl;
                log.info("[GoogleAuth] 리다이렉트 URL 설정: {}", finalRedirectUrl);
                
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("code", code);
                params.add("client_id", clientId);
                params.add("client_secret", clientSecret);
                params.add("redirect_uri", finalRedirectUrl);
                params.add("grant_type", "authorization_code");
                
                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
                log.info("[GoogleAuth] 구글 Access Token 요청 시작");
                
                ResponseEntity<String> response = restTemplate.exchange(GOOGLE_TOKEN_URL, HttpMethod.POST, requestEntity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    String accessToken = jsonNode.get("access_token").asText();
                    
                    // 액세스 토큰을 캐시에 저장
                    cacheService.cacheAccessTokenByCode(code, accessToken);
                    
                    log.info("[GoogleAuth] 구글 Access Token 발급 성공");
                    return accessToken;
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.warn("[GoogleAuth] API 요청 한도 초과, 재시도 {}/{}", retryCount, MAX_RETRY_COUNT);
                    if (retryCount == MAX_RETRY_COUNT) {
                        log.error("[GoogleAuth] 최대 재시도 횟수 초과: {}", e.getMessage());
                        throw new RuntimeException("구글 API 요청 한도를 초과했습니다.", e);
                    }
                    // 다음 재시도를 위해 계속
                    continue;
                } else {
                    log.error("[GoogleAuth] HTTP 에러: {}", e.getMessage());
                    throw new RuntimeException("구글 API 통신 중 오류가 발생했습니다: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                log.error("[GoogleAuth] 일반 에러: {}", e.getMessage());
                throw new RuntimeException("구글 API 통신 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }
        
        throw new RuntimeException("구글 액세스 토큰을 가져오는데 실패했습니다.");
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_URL, HttpMethod.GET, entity, String.class);
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            // 사용자 식별자 (이후 캐싱을 위해 사용)
            String socialId = rootNode.path("id").asText();
            String email = rootNode.path("email").asText();
            String username = rootNode.path("name").asText();
            String picture = rootNode.path("picture").asText();
            
            // 인증 정보에 성공하면 사용자 ID로 토큰을 캐싱
            if (!socialId.isEmpty()) {
                cacheService.cacheAccessTokenByUserId(socialId, accessToken);
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("socialId", socialId);
            userInfo.put("email", email);
            userInfo.put("username", username);
            userInfo.put("profileImage", picture);
            
            return userInfo;
        } catch (Exception e) {
            log.error("[GoogleAuth] 사용자 정보 요청 중 오류: {}", e.getMessage());
            throw new RuntimeException("구글 API에서 사용자 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
