package sweep.demo.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sweep.demo.SocialType;
import sweep.demo.Swagger.dto.auth.SocialUserResponseDto;
import sweep.demo.entity.auth.AuthUser;
import sweep.demo.global.security.JwtUtil;
import sweep.demo.repository.auth.UserRepository;

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
        // 1. 카카오에서 OAuth2 Access Token 발급
        String kakaoAccessToken = kakaoAuthImpl.getAccessToken(code);
        // 2. 카카오에서 사용자 정보 가져오기
        Map<String, Object> userInfo = kakaoAuthImpl.getUserInfo(kakaoAccessToken);
        return saveOrUpdateUser(userInfo, kakaoAccessToken, SocialType.KAKAO);
    }

    //2. 구글 로그인 처리
    public SocialUserResponseDto loginWithGoogle(String code) {
        // 1. 구글에서 OAuth2 Access Token 발급
        String googleAccessToken = googleAuthImpl.getAccessToken(code);
        // 2. 구글에서 사용자 정보 가져오기
        Map<String, Object> userInfo = googleAuthImpl.getUserInfo(googleAccessToken);
        return saveOrUpdateUser(userInfo, googleAccessToken, SocialType.GOOGLE);
    }

    // 3. 사용자 정보 저장 또는 업데이트 (카카오 & 구글)
    public SocialUserResponseDto saveOrUpdateUser(Map<String, Object> userInfo, String accessToken, SocialType socialType) {
        System.out.println("[AuthService] saveOrUpdateUser() 호출됨");
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
            System.out.println("기존 사용자 확인: " + user.getUsername());
            user.setAccessToken(accessToken);
        } else {
            // 3-2. 신규 사용자 → DB에 저장
            System.out.println("신규 사용자 저장: " + username);
            user = new AuthUser(socialId, username, email, accessToken, socialType);
        }
        // 4. 사용자 정보 저장 (기존 사용자도 Access Token 업데이트)
        userRepository.save(user);
        // 5. JWT Access Token & Refresh Token 생성
        SocialUserResponseDto userResponse = new SocialUserResponseDto(user);
        return generateJwtTokens(userResponse);
    }

    //4. JWT AccessToken & RefreshToken 생성 및 저장 (이후 응답 DTO에 추가함)
    public SocialUserResponseDto generateJwtTokens(SocialUserResponseDto userResponse) {
        // 1) JWT Access Token & Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(userResponse.getSocialId());
        String refreshToken = jwtUtil.generateRefreshToken(userResponse.getSocialId());
        // 2) Refresh Token을 DB에 저장
        Optional<AuthUser> optionalUser = userRepository.findBySocialId(userResponse.getSocialId());
        optionalUser.ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        });
        // 3) JWT 토큰을 응답 DTO에 포함
        userResponse.setTokens(accessToken, refreshToken);
        return userResponse;
    }
}


