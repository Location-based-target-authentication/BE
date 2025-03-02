package com.swyp.users.service;

import com.swyp.users.domain.User;
import com.swyp.users.repository.UserManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAgreementService {

    private final UserManagementRepository userRepository;

    @Transactional(readOnly = true)
    public boolean getPrivacyAgreement(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return user.isPrivacyAgreement();
    }

    @Transactional
    public void agreeToPrivacyPolicy(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setPrivacyAgreement(true);
        user.setPrivacyAgreementAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void agreeToTerms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setTermsAgreement(true);
        user.setTermsAgreementAt(LocalDateTime.now());
        userRepository.save(user);
    }
}