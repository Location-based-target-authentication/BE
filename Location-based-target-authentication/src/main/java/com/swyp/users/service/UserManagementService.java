package com.swyp.users.service;

import com.swyp.social_login.entity.AuthUser;
import com.swyp.users.domain.User;
import com.swyp.users.repository.UserManagementRepository;
import com.swyp.point.repository.PointRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.goal.repository.GoalAchievementsRepository;
import com.swyp.users.dto.UserModifyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserManagementRepository userRepository;
    private final PointRepository pointRepository;
    private final GoalRepository goalRepository;
    private final GoalAchievementsRepository goalAchievementsRepository;

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 로그아웃 처리 로직 구현
    }

    @Transactional(readOnly = true)
    public User getUserInfo(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public User modifyUserInfo(Long userId, UserModifyRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 연관된 데이터 삭제
        AuthUser authUser = user.getAuthUser();
        if (authUser != null) {
            pointRepository.deleteByAuthUser(authUser);
        }
        goalAchievementsRepository.deleteAllByUserId(userId);
        goalRepository.deleteAllByUserId(userId);
        
        userRepository.delete(user);
    }
} 