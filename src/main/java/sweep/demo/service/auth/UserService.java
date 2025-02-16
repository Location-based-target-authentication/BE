package sweep.demo.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sweep.demo.SocialType;
import sweep.demo.Swagger.dto.auth.SocialUserResponseDto;
import sweep.demo.entity.auth.AuthUser;
import sweep.demo.repository.auth.UserRepository;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KakaoAuthImpl kakaoAuthImpl;
    private final GoogleAuthImpl googleAuthImpl;

    //0. 받은 사용자 정보 저장
    public SocialUserResponseDto saveUser(Map<String, Object> userInfo, String accessToken, SocialType socialType) {
        // socialId (카카오 ID 또는 구글 ID)
        String socialId = userInfo.getOrDefault("socialId", "").toString();
        System.out.println("socialId 값 확인: " + socialId);
        String username = userInfo.getOrDefault("username", "Unknown").toString();
        String email = userInfo.getOrDefault("email", "").toString();

        // DB에서 기존 사용자 확인
        Optional<AuthUser> optionalUser = userRepository.findBySocialId(socialId);
        // 신규 사용자 저장 또는 기존 사용자 정보 업데이트
        AuthUser user = optionalUser.orElseGet(() -> {
            AuthUser newUser = new AuthUser(socialId, username, email, accessToken, socialType);
            return userRepository.save(newUser);
        });

        // 기존 사용자가 존재하면 Access Token 업데이트 후 저장
        if (optionalUser.isPresent()) {
            System.out.println("기존 사용자: " + user.getUsername());
            user.setAccessToken(accessToken);
            userRepository.save(user);
        } else {
            System.out.println("사용자 저장 완료: " + user.getUsername());
        }
        System.out.println("USER 저장됨");
        return new SocialUserResponseDto(user);
    }
    //1. 저장된 AccessToken으로 사용자 정보 가져오기 (카카오/구글 자동 구분)
    public Map<String, Object> getUserInfoByAccessToken(String accessToken, SocialType socialType) {
        if (socialType == SocialType.KAKAO) {
            return kakaoAuthImpl.getUserInfo(accessToken);
        } else if (socialType == SocialType.GOOGLE) {
            return googleAuthImpl.getUserInfo(accessToken);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 타입임");
        }
    }
    //2. 사용자 ID로 DB에서 AccessToken 조회 후 사용자 정보 가져오기
    public SocialUserResponseDto getUserInfoFromDb(String socialId) {
        Optional<AuthUser> userOptional = userRepository.findBySocialId(socialId);
        if (userOptional.isEmpty()) {
            System.out.println("[UserService] DB에서 socialId(" + socialId + ")를 가진 사용자를 찾을 수 없음!");
            return null;
        }

        AuthUser user = userOptional.get();
        // 데이터 확인용 로그 추가
        System.out.println("[UserService] DB에서 가져온 사용자: " + user.getUsername());
        System.out.println("[UserService] email: " + user.getEmail());
        System.out.println("[UserService] phoneNumber: " + user.getPhoneNumber());

        return userOptional.map(SocialUserResponseDto::new).orElse(null);
    }


    // 전화번호 저장 메서드
    public SocialUserResponseDto savePhoneNumber(String socialId, String phoneNumber) {
        Optional<AuthUser> optionalUser = userRepository.findBySocialId(socialId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("해당하는 socialId의 user가 없음: " + socialId);
        }
        AuthUser user = optionalUser.get();
        user.setPhoneNumber(phoneNumber); // 전화번호 업데이트
        userRepository.save(user); // DB 저장
        return new SocialUserResponseDto(user); // 업데이트된 사용자 정보 반환
    }
}