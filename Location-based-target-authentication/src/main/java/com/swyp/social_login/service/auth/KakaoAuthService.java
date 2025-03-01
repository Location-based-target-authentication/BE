package com.swyp.social_login.service.auth;

import java.util.Map;

public interface KakaoAuthService {
    String getAccessToken(String code, String referer);
    Map<String, Object> getUserInfo(String accessToken);
}
