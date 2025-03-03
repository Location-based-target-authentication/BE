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
    public SocialUserResponseDto saveOrUpdateUser(Map<String, Object> userInfo, String accessToken, SocialType socialType) {
        System.out.println("[AuthService] saveOrUpdateUser 시작");
        System.out.println("- userInfo: " + userInfo);
        System.out.println("- socialType: " + socialType);
        
        // 필수 사용자 정보
        String socialId = userInfo.getOrDefault("socialId", "").toString();
        String username = userInfo.getOrDefault("username", "Unknown").toString();
        String email = userInfo.getOrDefault("email", "").toString();
        
        System.out.println("- 추출된 정보:");
        System.out.println("  - socialId: " + socialId);
        System.out.println("  - username: " + username);
        System.out.println("  - email: " + email);
        
        // DB에서 기존 사용자 확인 (socialId와 email 모두 체크)
        System.out.println("[AuthService] DB에서 사용자 검색 시작");
        System.out.println("- socialId: " + socialId);
        System.out.println("- email: " + email);
        
        Optional<AuthUser> optionalUserBySocialId = userRepository.findBySocialId(socialId);
        Optional<AuthUser> optionalUserByEmail = userRepository.findByEmail(email);
        AuthUser user;
        
        if (optionalUserBySocialId.isPresent()) {
            // socialId로 찾은 사용자가 있는 경우
            System.out.println("[AuthService] socialId로 기존 사용자 발견");
            user = optionalUserBySocialId.get();
            user.setAccessToken(accessToken);
            System.out.println("- Access Token 업데이트 완료");
        } else if (optionalUserByEmail.isPresent()) {
            // 이메일은 있지만 다른 소셜 계정으로 가입한 경우
            System.out.println("[AuthService] 다른 소셜 계정으로 이미 가입된 이메일");
            AuthUser existingUser = optionalUserByEmail.get();
            throw new DuplicateEmailException(String.format("이미 %s로 가입된 이메일입니다. %s로 로그인해주세요.",
                existingUser.getSocialType(), existingUser.getSocialType()));
        } else {
            // 완전히 새로운 사용자인 경우
            System.out.println("[AuthService] 신규 사용자 등록 시작");
            user = new AuthUser(socialId, username, email, accessToken, socialType);
            
            // userId를 저장하기 전에 설정 (id와 동일하게)
            user.setUserId(user.getId());
            
            // 새로운 사용자 저장
            user = userRepository.save(user);
            System.out.println("- 사용자 저장 완료 (id: " + user.getId() + ", userId: " + user.getUserId() + ")");
            
            Point point = new Point(user);
            point.setTotalPoints(2000);
            pointRepository.save(point);
            System.out.println("- 초기 포인트 2000점 설정 완료");
        }
        
        // 사용자 정보를 final 변수에 저장
        final AuthUser savedUser = user;
        
        //기존 유저라도 포인트 없으면 생성
        System.out.println("[AuthService] 포인트 정보 확인");
        Point point = pointRepository.findByAuthUser(savedUser).orElseGet(() -> {
            System.out.println("- 포인트 정보 없음, 새로 생성");
            return pointRepository.save(new Point(savedUser));
        });
        System.out.println("- 현재 포인트: " + point.getTotalPoints());
        
        // JWT Access Token & Refresh Token 생성
        SocialUserResponseDto userResponse = new SocialUserResponseDto(user);
        return generateJwtTokens(userResponse);
    }

    //JWT AccessToken & RefreshToken 생성 및 저장
    public SocialUserResponseDto generateJwtTokens(SocialUserResponseDto userResponse) {
        System.out.println("[AuthService] JWT 토큰 생성 시작");
        // DB에서 사용자 조회 (데이터베이스 ID 사용)
        Long userId = userResponse.getUserId();
        System.out.println("- userId: " + userId);
        
        if (userId == null) {
            System.out.println("[AuthService] userId가 null입니다");
            throw new IllegalStateException("유효하지 않은 userId입니다.");
        }
        
        Optional<AuthUser> optionalUser = userRepository.findByUserId(userId);
        if (optionalUser.isEmpty()) {
            System.out.println("[AuthService] 사용자 찾기 실패");
            throw new IllegalStateException("사용자를 찾을 수 없습니다.");
        }
        System.out.println("[AuthService] 사용자 찾기 성공");
        AuthUser user = optionalUser.get();
        
        // userId와 id가 일치하는지 확인
        if (!user.getId().equals(user.getUserId())) {
            System.out.println("[AuthService] userId와 id가 일치하지 않습니다. 수정합니다.");
            user.setUserId(user.getId());
            user = userRepository.save(user);
        }
        
        // AuthUser 객체로 토큰 생성
        System.out.println("[AuthService] 토큰 생성 시작");
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        System.out.println("- Access Token 생성 완료");
        System.out.println("- Refresh Token 생성 완료");
        
        // Refresh Token을 DB에 저장
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        System.out.println("- Refresh Token DB 저장 완료");
        
        userResponse.setTokens(accessToken, refreshToken);
        System.out.println("[AuthService] JWT 토큰 생성 완료");
        return userResponse;
    }
}


