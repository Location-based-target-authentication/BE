package com.swyp.social_login.service.auth;

import com.swyp.global.security.JwtUtil;
import com.swyp.point.entity.Point;
import com.swyp.point.repository.PointRepository;
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
    private final PointRepository pointRepository;
    private final KakaoAuthService kakaoAuthService;
    private final GoogleAuthService googleAuthService;
    public SocialUserResponseDto loginWithKakao(String code) {
        // 1. 카카오에서 OAuth2 Access Token 발급
        String kakaoAccessToken = kakaoAuthService.getAccessToken(code);
        // 2. 카카오에서 사용자 정보 가져오기
        Map<String, Object> userInfo = kakaoAuthService.getUserInfo(kakaoAccessToken);
        return saveOrUpdateUser(userInfo, kakaoAccessToken, SocialType.KAKAO);
    }

    //2. 구글 로그인 처리
    public SocialUserResponseDto loginWithGoogle(String code) {
        String googleAccessToken = googleAuthService.getAccessToken(code);
        Map<String, Object> userInfo = googleAuthService.getUserInfo(googleAccessToken);
        return saveOrUpdateUser(userInfo, googleAccessToken, SocialType.GOOGLE);
    }
    // 사용자 정보 저장 또는 업데이트 (카카오 & 구글)
    public SocialUserResponseDto saveOrUpdateUser(Map<String, Object> userInfo, String accessToken, SocialType socialType) {
        // 필수 사용자 정보
        String userId = userInfo.getOrDefault("userId", "").toString();
        String username = userInfo.getOrDefault("username", "Unknown").toString();
        String email = userInfo.getOrDefault("email", "").toString();
        
        // DB에서 기존 사용자 확인
        Optional<AuthUser> optionalUser = userRepository.findByUserId(userId);
        AuthUser user;
        if (optionalUser.isPresent()) {// 3-1. 기존 사용자 → Access Token 업데이트
            user = optionalUser.get();
            user.setAccessToken(accessToken);
        } else {// 3-2. 신규 사용자 → DB에 저장
            user = new AuthUser(userId, username, email, accessToken, socialType);
            userRepository.save(user);
            Point point = new Point(user);
            point.setTotalPoints(2000);
            pointRepository.save(point);
        }
        
        //기존 유저라도 포인트 없으면 생성
        pointRepository.findByAuthUser(user).orElseGet(() -> pointRepository.save(new Point(user)));
        
        // JWT Access Token & Refresh Token 생성
        SocialUserResponseDto userResponse = new SocialUserResponseDto(user);
        return generateJwtTokens(userResponse);
    }

    //JWT AccessToken & RefreshToken 생성 및 저장
    public SocialUserResponseDto generateJwtTokens(SocialUserResponseDto userResponse) {
        // DB에서 사용자 조회 (데이터베이스 ID 사용)
        Long dbId = Long.parseLong(userResponse.getUserId());
        Optional<AuthUser> optionalUser = userRepository.findById(dbId);
        if (optionalUser.isEmpty()) {
            throw new IllegalStateException("사용자를 찾을 수 없습니다.");
        }
        AuthUser user = optionalUser.get();
        
        // DB ID를 문자열로 변환하여 토큰 생성
        String dbId = String.valueOf(user.getId());
        String accessToken = jwtUtil.generateAccessToken(dbId);
        String refreshToken = jwtUtil.generateRefreshToken(dbId);
        
        // Refresh Token을 DB에 저장
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        
        userResponse.setTokens(accessToken, refreshToken);
        return userResponse;
    }
}


