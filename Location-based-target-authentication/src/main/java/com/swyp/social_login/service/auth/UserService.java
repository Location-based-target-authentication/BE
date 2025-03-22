package com.swyp.social_login.service.auth;

import com.swyp.social_login.dto.SocialUserResponseDto;
import com.swyp.social_login.entity.AuthUser;
import com.swyp.users.domain.User;
import com.swyp.social_login.repository.UserRepository;
import com.swyp.users.repository.UserManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserManagementRepository userManagementRepository;
    
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

    /**
     * 사용자 저장 시 User 테이블과 연결 설정
     */
    @Transactional
    public AuthUser saveUser(AuthUser authUser) {
        // 사용자 저장
        AuthUser savedUser = userRepository.save(authUser);
        
        // User 테이블에 연결된 엔터티가 있는지 확인
        Optional<User> userOptional = userManagementRepository.findByUserId(savedUser.getUserId());
        
        if (userOptional.isPresent()) {
            // 기존 User 엔터티가 있으면 auth_user_id 업데이트
            User user = userOptional.get();
            user.setAuthUser(savedUser);
            userManagementRepository.save(user);
        } else {
            // 없으면 새로 생성하지 않음 - 소셜 로그인 과정에서 이미 생성됨
            System.out.println("User not found for AuthUser ID: " + savedUser.getUserId());
        }
        
        return savedUser;
    }
    
    /**
     * 애플리케이션 시작 시 모든 기존 사용자의 auth_user_id 설정
     * ApplicationRunner를 통해 호출됨
     */
    @Transactional
    public void linkUserEntities() {
        // 모든 사용자를 조회
        List<AuthUser> allAuthUsers = userRepository.findAll();
        
        for (AuthUser authUser : allAuthUsers) {
            // 해당 AuthUser에 연결된 User 엔터티가 있는지 확인
            Optional<User> userOptional = userManagementRepository.findByUserId(authUser.getUserId());
            
            if (userOptional.isPresent()) {
                // 기존 User 엔터티가 있으면 auth_user_id 업데이트
                User user = userOptional.get();
                if (user.getAuthUser() == null) {
                    user.setAuthUser(authUser);
                    userManagementRepository.save(user);
                    System.out.println("User " + user.getId() + " 연결 완료: auth_user_id = " + authUser.getId());
                }
            }
        }
        System.out.println("모든 사용자 연결 작업 완료");
    }
}