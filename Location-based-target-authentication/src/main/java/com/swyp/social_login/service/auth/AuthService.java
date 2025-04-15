package com.swyp.social_login.service.auth;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swyp.global.cache.AuthRequestCacheService;
import com.swyp.global.security.JwtUtil;
import com.swyp.point.entity.Point;
import com.swyp.point.repository.PointRepository;
import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.enums.SocialType;
import com.swyp.social_login.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final KakaoAuthService kakaoAuthService;
    private final GoogleAuthService googleAuthService;
    private final AuthRequestCacheService cacheService;
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    public SocialUserResponseDto loginWithKakao(String code) {
        try {
            // 1. 카카오에서 OAuth2 Access Token 발급
            String kakaoAccessToken = kakaoAuthService.getAccessToken(code);
            // 2. 카카오에서 사용자 정보 가져오기
            Map<String, Object> userInfo = kakaoAuthService.getUserInfo(kakaoAccessToken);
            return saveOrUpdateUser(userInfo, kakaoAccessToken, SocialType.KAKAO);
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 구글 로그인 처리
    public SocialUserResponseDto loginWithGoogle(String code) {
        try {
            String googleAccessToken = googleAuthService.getAccessToken(code);
            Map<String, Object> userInfo = googleAuthService.getUserInfo(googleAccessToken);
            return saveOrUpdateUser(userInfo, googleAccessToken, SocialType.GOOGLE);
        } catch (Exception e) {
            log.error("구글 로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // 사용자 정보 저장 또는 업데이트 (카카오 & 구글)
    @Transactional
    public SocialUserResponseDto saveOrUpdateUser(Map<String, Object> userInfo, String accessToken, SocialType socialType) {
        String socialId = userInfo.getOrDefault("socialId", "").toString();
        String username = userInfo.getOrDefault("username", "Unknown").toString();
        String email = userInfo.getOrDefault("email", "").toString();
        
        // 캐시에서 사용자 ID로 액세스 토큰 조회 시도
        String cachedToken = cacheService.getAccessTokenByUserId(socialId);
        if (cachedToken != null) {
            log.info("사용자 ID {} 에 대한 액세스 토큰이 캐시에 존재합니다. 기존 토큰을 사용합니다.", socialId);
            accessToken = cachedToken;
        }
        
        // 기존 사용자 확인 - socialId와 socialType으로 확인
        Optional<AuthUser> existingUser = userRepository.findBySocialIdAndSocialType(socialId, socialType);

        AuthUser user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setAccessToken(accessToken);
            user = userRepository.save(user);
            
            // 캐시 업데이트
            cacheService.cacheAccessTokenByUserId(socialId, accessToken);
            log.info("기존 사용자 {}의 액세스 토큰이 업데이트되었습니다.", user.getUserId());
        } else {
            // SocialId로 찾지 못했다면 이메일로 찾아봄
            Optional<AuthUser> userByEmail = userRepository.findByEmail(email);
            
            if (userByEmail.isPresent()) {
                // 동일한 이메일을 가진 사용자가 있다면 해당 사용자 정보 업데이트
                user = userByEmail.get();
                log.info("동일한 이메일 {}를 가진 사용자를 찾았습니다. 기존 계정에 소셜 로그인 정보를 연결합니다.", email);
                
                // 기존 사용자의 소셜 정보 업데이트
                user.setSocialId(socialId);
                user.setSocialType(socialType);
                user.setAccessToken(accessToken);
                
                // 다른 정보도 업데이트 필요시 추가
                if (user.getUsername() == null || user.getUsername().isEmpty()) {
                    user.setUsername(username);
                }
                
                user = userRepository.save(user);
                
                // 캐시 업데이트
                cacheService.cacheAccessTokenByUserId(socialId, accessToken);
                log.info("기존 사용자 {}의 소셜 로그인 정보가 업데이트되었습니다.", user.getUserId());
            } else {
                // 완전히 새로운 사용자인 경우
                user = AuthUser.builder()
                        .socialId(socialId)
                        .username(username)
                        .name(username)  // name 필드를 username으로 초기화
                        .email(email)
                        .accessToken(accessToken)
                        .socialType(socialType)
                        .userId(0L)  // 임시 userId 설정
                        .build();
                
                user = userRepository.save(user);
                
                // 새 사용자의 경우 캐시에 저장
                cacheService.cacheAccessTokenByUserId(socialId, accessToken);
                log.info("새 사용자 {}가 등록되었습니다.", user.getUserId());
                
                Point point = new Point(user);
                point.setTotalPoints(2000);
                pointRepository.save(point);
            }
        }

        // 포인트 정보 확인 및 생성
        if (!pointRepository.findByAuthUser(user).isPresent()) {
            Point point = new Point(user);
            point.setTotalPoints(0);
            pointRepository.save(point);
        }

        return generateJwtTokens(new SocialUserResponseDto(user));
    }

    // JWT AccessToken & RefreshToken 생성 및 저장
    @Transactional
    public SocialUserResponseDto generateJwtTokens(SocialUserResponseDto userResponse) {
        Long userId = userResponse.getUserId();
        if (userId == null) {
            throw new IllegalStateException("유효하지 않은 userId입니다.");
        }

        AuthUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        userResponse.setTokens(accessToken, refreshToken);
        return userResponse;
    }
}


