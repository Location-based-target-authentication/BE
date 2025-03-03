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
    
    public SocialUserResponseDto getUserInfoFromDb(Long userId) {
        Optional<AuthUser> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            return null;
        }
        AuthUser user = userOptional.get();
        return userOptional.map(SocialUserResponseDto::new).orElse(null);
    }

    // 전화번호 저장 메서드
    public SocialUserResponseDto savePhoneNumber(Long userId, String phoneNumber) {
        Optional<AuthUser> optionalUser = userRepository.findByUserId(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("해당하는 userId의 user가 없음: " + userId);
        }
        AuthUser user = optionalUser.get();
        user.setPhoneNumber(phoneNumber); // 전화번호 업데이트
        userRepository.save(user); // DB 저장
        return new SocialUserResponseDto(user); // 업데이트된 사용자 정보 반환
    }
}