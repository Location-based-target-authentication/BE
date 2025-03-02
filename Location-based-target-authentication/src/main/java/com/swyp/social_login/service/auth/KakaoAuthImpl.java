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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoAuthImpl implements KakaoAuthService {
    private final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    @Value("${kakao.client.id}")
    private String KAKAO_CLIENT_ID;
    @Value("${kakao.client.secret}")
    private String KAKAO_CLIENT_SECRET;
    @Value("${kakao.redirect.url}")
    private String KAKAO_REDIRECT_URL;
    @Value("${kakao.redirect.url.local}")
    private String KAKAO_REDIRECT_URL_LOCAL;

    private final HttpServletRequest request;

    public KakaoAuthImpl(HttpServletRequest request) {
        this.request = request;
    }

    // 1. OAuth2 Access Token 발급
    @Override
    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null && referer.contains("localhost")) 
            ? KAKAO_REDIRECT_URL_LOCAL 
            : KAKAO_REDIRECT_URL;

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", KAKAO_CLIENT_ID);
        params.add("client_secret", KAKAO_CLIENT_SECRET);
        params.add("redirect_uri", redirectUrl);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, request, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("카카오 Access Token 요청 실패", e);
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(KAKAO_USER_INFO_URL, HttpMethod.GET, entity, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", jsonNode.get("id").asText());
            userInfo.put("email", jsonNode.get("kakao_account").get("email").asText());
            userInfo.put("username", jsonNode.get("kakao_account").get("profile").get("nickname").asText());

            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 요청 실패", e);
        }
    }
}


