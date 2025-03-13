package com.swyp.social_login.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleAuthImpl implements GoogleAuthService {
    private final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUrl;
    @Value("${google.redirect.url.local}")
    private String redirectUrlLocal;

    private final HttpServletRequest request;

    public GoogleAuthImpl(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getAccessToken(String code) {
        System.out.println("[GoogleAuthImpl] getAccessToken 시작 - code: " + code);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String finalRedirectUrl = redirectUrl;
        System.out.println("[GoogleAuthImpl] 선택된 redirectUrl: " + finalRedirectUrl);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", finalRedirectUrl);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(GOOGLE_TOKEN_URL, HttpMethod.POST, request, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();
            System.out.println("[GoogleAuthImpl] Access Token 발급 성공");
            return accessToken;
        } catch (Exception e) {
            System.out.println("[GoogleAuthImpl] Access Token 발급 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Google Access Token 요청 실패", e);
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        System.out.println("[GoogleAuthImpl] getUserInfo 시작");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_URL, HttpMethod.GET, entity, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            Map<String, Object> userInfo = new HashMap<>();
            String socialId = jsonNode.get("id").asText();
            String email = jsonNode.get("email").asText();
            String username = jsonNode.get("name").asText();
            
            System.out.println("[GoogleAuthImpl] 사용자 정보 파싱 결과:");
            System.out.println("- socialId: " + socialId);
            System.out.println("- email: " + email);
            System.out.println("- username: " + username);
            
            userInfo.put("socialId", socialId);
            userInfo.put("email", email);
            userInfo.put("username", username);

            System.out.println("[GoogleAuthImpl] 사용자 정보 조회 성공");
            return userInfo;
        } catch (Exception e) {
            System.out.println("[GoogleAuthImpl] 사용자 정보 조회 실패: " + e.getMessage());
            System.out.println("[GoogleAuthImpl] 응답 내용: " + response.getBody());
            e.printStackTrace();
            throw new RuntimeException("Google 사용자 정보 요청 실패", e);
        }
    }
}
