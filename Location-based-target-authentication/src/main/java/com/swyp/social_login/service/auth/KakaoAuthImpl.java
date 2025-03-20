package com.swyp.social_login.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoAuthImpl implements KakaoAuthService {
    private final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";

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
    public TokenDto getAccessToken(String code) {
        log.info("카카오 인증 코드: {}", code);
        log.info("사용하는 리다이렉트 URI: {}", redirectUri);
        
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

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        System.out.println("[KakaoAuth] Access Token 요청 시작");

        ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, request, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();
            System.out.println("[KakaoAuth] Access Token 발급 성공");
            return new TokenDto(accessToken);
        } catch (Exception e) {
            System.err.println("[KakaoAuth] Access Token 발급 실패: " + e.getMessage());
            throw new RuntimeException("카카오 Access Token 요청 실패", e);
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        System.out.println("[KakaoAuth] 사용자 정보 조회 시작");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
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
            System.out.println("[KakaoAuth] socialId: " + socialId);
            System.out.println("[KakaoAuth] email: " + email);
            System.out.println("[KakaoAuth] username: " + username);

            return userInfo;
        } catch (Exception e) {
            System.err.println("[KakaoAuth] 사용자 정보 조회 실패: " + e.getMessage());
            throw new RuntimeException("카카오 사용자 정보 요청 실패", e);
        }
    }
}


