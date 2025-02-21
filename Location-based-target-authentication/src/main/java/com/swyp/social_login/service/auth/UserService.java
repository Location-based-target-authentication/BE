package com.swyp.social_login.service.auth;

import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.social_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public SocialUserResponseDto getUserInfoFromDb(String socialId) {
        Optional<AuthUser> userOptional = userRepository.findBySocialId(socialId);
        if (userOptional.isEmpty()) {
            return null;
        }
        AuthUser user = userOptional.get();
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