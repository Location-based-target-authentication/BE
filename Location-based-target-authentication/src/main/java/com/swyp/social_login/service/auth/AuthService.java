package com.swyp.social_login.service.auth;

import com.swyp.global.security.JwtUtil;
import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final KakaoAuthImpl kakaoAuthImpl;
    private final GoogleAuthImpl googleAuthImpl;


    //1. 카카오 로그인 처리
    public SocialUserResponseDto loginWithKakao(String code) {
        System.out.println("loginWithKakao로 받은 code: " + code);
        // 1. 카카오에서 OAuth2 Access Token 발급
        String kakaoAccessToken = kakaoAuthImpl.getAccessToken(code);
        System.out.println("발급된 OAuth access token: " + kakaoAccessToken);
        // 2. 카카오에서 사용자 정보 가져오기
        Map<String, Object> userInfo = kakaoAuthImpl.getUserInfo(kakaoAccessToken);
        return saveOrUpdateUser(userInfo, kakaoAccessToken, SocialType.KAKAO);
    }

    //2. 구글 로그인 처리
    public SocialUserResponseDto loginWithGoogle(String code) {
        System.out.println("loginWithGoogle로 받은 code: " + code);
        // 1. 구글에서 OAuth2 Access Token 발급
        String googleAccessToken = googleAuthImpl.getAccessToken(code);
        System.out.println("발급된 OAuth access token: " + googleAccessToken);
        // 2. 구글에서 사용자 정보 가져오기
        Map<String, Object> userInfo = googleAuthImpl.getUserInfo(googleAccessToken);
        return saveOrUpdateUser(userInfo, googleAccessToken, SocialType.GOOGLE);
    }
    // 사용자 정보 저장 또는 업데이트 (카카오 & 구글)
    public SocialUserResponseDto saveOrUpdateUser(Map<String, Object> userInfo, String accessToken, SocialType socialType) {
        // 필수 사용자 정보 가져오기
        String socialId = userInfo.getOrDefault("socialId", "").toString();
        String username = userInfo.getOrDefault("username", "Unknown").toString();
        String email = userInfo.getOrDefault("email", "").toString();
        // DB에서 기존 사용자 확인
        Optional<AuthUser> optionalUser = userRepository.findBySocialId(socialId);
        AuthUser user;
        if (optionalUser.isPresent()) {
            // 3-1. 기존 사용자 → Access Token 업데이트
            user = optionalUser.get();
            user.setAccessToken(accessToken);
        } else {
            // 3-2. 신규 사용자 → DB에 저장
            user = new AuthUser(socialId, username, email, accessToken, socialType);
        }
        userRepository.save(user);
        // JWT Access Token & Refresh Token 생성
        SocialUserResponseDto userResponse = new SocialUserResponseDto(user);
        return generateJwtTokens(userResponse);
    }

    //JWT AccessToken & RefreshToken 생성 및 저장
    public SocialUserResponseDto generateJwtTokens(SocialUserResponseDto userResponse) {
        String accessToken = jwtUtil.generateAccessToken(userResponse.getSocialId());
        String refreshToken = jwtUtil.generateRefreshToken(userResponse.getSocialId());
        // Refresh Token을 DB에 저장
        Optional<AuthUser> optionalUser = userRepository.findBySocialId(userResponse.getSocialId());
        optionalUser.ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        });
        userResponse.setTokens(accessToken, refreshToken);
        return userResponse;
    }
}


