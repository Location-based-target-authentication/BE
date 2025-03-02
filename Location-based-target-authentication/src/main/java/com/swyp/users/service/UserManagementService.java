package com.swyp.users.service;

import com.swyp.social_login.entity.AuthUser;
import com.swyp.users.domain.User;
import com.swyp.users.repository.UserManagementRepository;
import com.swyp.point.repository.PointRepository;
import com.swyp.point.repository.PointHistoryRepository;
import com.swyp.goal.repository.GoalRepository;
import com.swyp.goal.repository.GoalAchievementsRepository;
import com.swyp.users.dto.UserModifyRequest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserManagementRepository userRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final GoalRepository goalRepository;
    private final GoalAchievementsRepository goalAchievementsRepository;
    private final EntityManager entityManager;

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 로그아웃 처리 로직 구현
    }

    @Transactional(readOnly = true)
    public User getUserInfo(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
        
        // 먼저 ID로 조회 시도
        Optional<User> userById = userRepository.findByIdWithAuthUser(userId);
        if (userById.isPresent()) {
            return userById.get();
        }
        
        // userId로 조회 시도
        return userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
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
        
        try {
            // 연관된 데이터 삭제
            // 1. 포인트 히스토리 삭제
            pointHistoryRepository.deleteByAuthUserId(userId);
            entityManager.flush();
            entityManager.clear();
            
            // 2. 포인트 삭제
            pointRepository.deleteAllByAuthUser_Id(userId);
            entityManager.flush();
            entityManager.clear();
            
            // 3. 목표 달성 기록 삭제
            goalAchievementsRepository.deleteAllByUserId(userId);
            entityManager.flush();
            entityManager.clear();
            
            // 4. 목표 삭제 (목표 반복 요일은 ON DELETE CASCADE로 자동 삭제)
            goalRepository.deleteAllByUserId(userId);
            entityManager.flush();
            entityManager.clear();
            
            // 5. 사용자 삭제
            userRepository.delete(user);
        } catch (Exception e) {
            throw new RuntimeException("회원 탈퇴 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
} 