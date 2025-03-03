package com.swyp.social_login.service.auth;

import com.swyp.global.security.JwtUtil;
import com.swyp.point.entity.Point;
import com.swyp.point.repository.PointRepository;
import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.exception.DuplicateEmailException;
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
    @Transactional
    public SocialUserResponseDto saveOrUpdateUser(Map<String, Object> userInfo, String accessToken, SocialType socialType) {
        String socialId = userInfo.getOrDefault("socialId", "").toString();
        String username = userInfo.getOrDefault("username", "Unknown").toString();
        String email = userInfo.getOrDefault("email", "").toString();
        
        // 기존 사용자 확인
        Optional<AuthUser> existingUserBySocialId = userRepository.findBySocialId(socialId);
        Optional<AuthUser> existingUserByEmail = userRepository.findByEmail(email);

        AuthUser user;
        if (existingUserBySocialId.isPresent()) {
            user = existingUserBySocialId.get();
            user.setAccessToken(accessToken);
            user = userRepository.save(user);
        } else if (existingUserByEmail.isPresent()) {
            AuthUser duplicateUser = existingUserByEmail.get();
            throw new DuplicateEmailException(String.format("이미 %s로 가입된 이메일입니다. %s로 로그인해주세요.",
                    duplicateUser.getSocialType(), duplicateUser.getSocialType()));
        } else {
            user = AuthUser.builder()
                    .socialId(socialId)
                    .username(username)
                    .email(email)
                    .accessToken(accessToken)
                    .socialType(socialType)
                    .build();
            
            user = userRepository.save(user);
            
            Point point = Point.builder()
                    .authUser(user)
                    .totalPoints(2000L)
                    .build();
            pointRepository.save(point);
        }

        // 포인트 정보 확인 및 생성
        pointRepository.findByAuthUser(user).orElseGet(() -> 
            pointRepository.save(Point.builder()
                    .authUser(user)
                    .totalPoints(0L)
                    .build()));

        return generateJwtTokens(new SocialUserResponseDto(user));
    }

    //JWT AccessToken & RefreshToken 생성 및 저장
    @Transactional
    public SocialUserResponseDto generateJwtTokens(SocialUserResponseDto userResponse) {
        Long userId = userResponse.getUserId();
        if (userId == null) {
            throw new IllegalStateException("유효하지 않은 userId입니다.");
        }

        AuthUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        userResponse.setTokens(accessToken, refreshToken);
        return userResponse;
    }
}


